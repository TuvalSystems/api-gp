package tuval.api.web.reportes.inventario.articulos.service.dynamics.imp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tuval.api.web.reportes.inventario.articulos.dao.dynamics.ArticulosDao;
import tuval.api.web.reportes.inventario.articulos.dto.ArticulosDto;
import tuval.api.web.reportes.inventario.articulos.dto.CatalogoArticuloDto;
import tuval.api.web.reportes.inventario.articulos.dto.ListadoItemsDto;
import tuval.api.web.reportes.inventario.articulos.dto.ResolucionArticulosDto;
import tuval.api.web.reportes.inventario.articulos.request.ArticulosRequest;
import tuval.api.web.reportes.inventario.articulos.request.ListadoItemsRequest;
import tuval.api.web.reportes.inventario.articulos.request.ResolucionArticulosRequest;
import tuval.api.web.reportes.inventario.articulos.service.dynamics.ArticulosService;

@Service
@RequiredArgsConstructor
public class ArticulosServiceImp implements ArticulosService {
  private final ArticulosDao articulosDao;
  private static final long VIGENCIA_INDICE_NANOS = 60_000_000_000L;
  private record CacheIndice(IndiceArticulos indice, long creado) {}
  private volatile CacheIndice cache;

  @Override
  public List<ArticulosDto> consultar(ArticulosRequest request) {
    if (request == null || request.getPage() == null || request.getPage() < 0
        || request.getSize() == null || request.getSize() < 1 || request.getSize() > 500
        || request.getSort() == null || request.getDirection() == null) {
      throw new IllegalArgumentException("Parámetros inválidos del reporte de artículos");
    }
    return articulosDao.consultar(request);
  }

  @Override
  public List<ListadoItemsDto> consultarListadoItems(ListadoItemsRequest request) {
    if (request == null || request.getPage() == null || request.getPage() < 0
        || request.getPage() > 10000 || request.getSize() == null
        || request.getSize() < 1 || request.getSize() > 500
        || !validText(request.getCodigo(), 31) || !validText(request.getDescripcion(), 101)
        || !validText(request.getMarca(), 100) || !validText(request.getMaterial(), 100)) {
      throw new IllegalArgumentException("Parametros invalidos del listado de items");
    }
    return articulosDao.consultarListadoItems(request);
  }

  private static boolean validText(String value, int maxLength) {
    return value == null || (!value.isBlank() && value.length() <= maxLength);
  }

  @Override
  public List<ResolucionArticulosDto> resolverArticulos(ResolucionArticulosRequest request) {
    validar(request);
    int maximo = request.maxCandidatos() == null ? 3 : request.maxCandidatos();
    Map<String, List<CatalogoArticuloDto>> codigos = new HashMap<>();
    List<ResolucionArticulosDto> resultado = new ArrayList<>();
    IndiceArticulos indice = null;
    for (var linea : request.detalle()) {
      String codigo = codigo(linea.texto());
      List<IndiceArticulos.CoincidenciaTexto> filas;
      if (codigo != null) {
        filas = codigos.computeIfAbsent(codigo, articulosDao::buscarCodigoResolucion).stream()
            .map(articulo -> new IndiceArticulos.CoincidenciaTexto(articulo, 100)).toList();
      } else {
        var consulta = BusquedaTextoArticulo.preparar(linea.texto());
        if (consulta.terminos().isEmpty()) filas = List.of();
        else {
          if (indice == null) indice = obtenerIndice();
          filas = indice.buscar(consulta, maximo);
        }
      }
      String estado = filas.isEmpty() ? "sin_coincidencia"
          : codigo != null && filas.size() == 1 ? "identificado" : "ambiguo";
      var coincidencias = filas.stream().limit(maximo).map(fila -> new ResolucionArticulosDto.Coincidencia(
          fila.articulo().descripcion(), fila.articulo().codigoGP(), fila.articulo().codigo3L(),
          BigDecimal.valueOf(fila.porcentaje()).setScale(2, RoundingMode.HALF_UP))).toList();
      resultado.add(new ResolucionArticulosDto(linea.texto(),
          linea.cantidad() == null ? BigDecimal.ONE : linea.cantidad(), estado, coincidencias));
    }
    return resultado;
  }

  private IndiceArticulos obtenerIndice() {
    var actual = cache;
    if (actual != null && System.nanoTime() - actual.creado() < VIGENCIA_INDICE_NANOS) return actual.indice();
    synchronized (this) {
      actual = cache;
      if (actual != null && System.nanoTime() - actual.creado() < VIGENCIA_INDICE_NANOS) return actual.indice();
      var filas = articulosDao.consultarCatalogoResolucion();
      if (filas.size() > ArticulosDao.LIMITE_CATALOGO_RESOLUCION) {
        throw new IllegalStateException("El catalogo supera el limite del indice de busqueda");
      }
      var indice = new IndiceArticulos(filas);
      cache = new CacheIndice(indice, System.nanoTime());
      return indice;
    }
  }

  private static String codigo(String texto) {
    String valor = BusquedaTextoArticulo.normalizar(texto);
    if (valor.matches("\\d{3}-\\d{6}-[A-Z]")) return valor;
    if (valor.matches("CODIGO [A-Z0-9][A-Z0-9._/-]{0,30}")) return valor.substring(7);
    return null;
  }

  private static void validar(ResolucionArticulosRequest request) {
    if (request == null || request.detalle() == null || request.detalle().isEmpty()
        || request.detalle().size() > 100 || (request.maxCandidatos() != null &&
        (request.maxCandidatos() < 1 || request.maxCandidatos() > 6))) throw new IllegalArgumentException();
    for (var line : request.detalle()) {
      if (line == null || line.texto() == null || line.texto().isBlank() || line.texto().length() > 200
          || (line.cantidad() != null && (line.cantidad().signum() <= 0
          || line.cantidad().compareTo(new BigDecimal("1000000")) > 0
          || line.cantidad().scale() > 3))) throw new IllegalArgumentException();
    }
  }
}
