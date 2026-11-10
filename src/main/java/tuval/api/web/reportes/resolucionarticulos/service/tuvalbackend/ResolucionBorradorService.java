package tuval.api.web.reportes.resolucionarticulos.service.tuvalbackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.security.MessageDigest;
import org.springframework.stereotype.Service;
import tuval.api.web.reportes.cotizacionborrador.service.tuvalbackend.CotizacionBorradorException;
import tuval.api.web.reportes.resolucionarticulos.client.BorradorItemsPruebasClient;
import tuval.api.web.reportes.resolucionarticulos.client.BorradorItemsPruebasClient.*;
import tuval.api.web.reportes.resolucionarticulos.dto.ResolucionArticulosReporteDto;
import tuval.api.web.reportes.resolucionarticulos.dto.ResolucionBorradorDto;
import tuval.api.web.reportes.resolucionarticulos.request.ResolucionArticulosReporteRequest;
import tuval.api.web.reportes.resolucionarticulos.request.ResolucionBorradorRequest;
import tuval.api.web.reportes.resolucionarticulos.service.dynamics.ResolucionArticulosReporteService;

@Service
public class ResolucionBorradorService {
  private final ResolucionArticulosReporteService resolver;
  private final BorradorItemsPruebasClient client;
  private final ObjectMapper mapper;
  // Safety brake only, NOT persistent/distributed idempotency. Never evict an uncertain operation.
  private final Map<String, Intento> intentos = new HashMap<>();
  private static final int MAX_INTENTOS = 1000;
  private static final class Intento {
    final String fingerprint;
    volatile Resultado result;
    Intento(String fingerprint) { this.fingerprint = fingerprint; }
  }

  public ResolucionBorradorService(ResolucionArticulosReporteService resolver,
      BorradorItemsPruebasClient client, ObjectMapper mapper) {
    this.resolver = resolver; this.client = client; this.mapper = mapper;
  }

  public Resultado crear(ResolucionBorradorRequest request) {
    client.verificarConfiguracion(); // No catalog access when this write integration is disabled.
    String id = request.requestId().toLowerCase(Locale.ROOT);
    String cliente = request.clienteRuc() == null ? "0990007020001" : request.clienteRuc();
    Long pv = request.pvId() == null ? 2L : request.pvId();
    String fingerprint = fingerprint(List.of(cliente, pv, request.renglones()));
    Intento intento;
    synchronized (intentos) {
      var previous = intentos.get(id);
      if (previous != null) {
        if (!previous.fingerprint.equals(fingerprint) || previous.result == null)
          throw new CotizacionBorradorException(409);
        var result = previous.result;
        return new Resultado(200, result.resoluciones(), result.borrador(), true, result.opcionesPorRenglon(), result.advertencias());
      }
      if (intentos.size() >= MAX_INTENTOS) throw new CotizacionBorradorException(503);
      intento = new Intento(fingerprint);
      intentos.put(id, intento);
    }
    boolean enviado = false;
    try {
      var consulta = new ResolucionArticulosReporteRequest(request.renglones().stream()
          .map(line -> new ResolucionArticulosReporteRequest.Renglon(line.texto(), line.cantidad())).toList(), 5);
      var resoluciones = resolver.consultar(consulta);
      var items = new LinkedHashMap<String, Item>();
      var opciones = new ArrayList<OpcionRenglon>();
      var advertencias = new LinkedHashSet<String>();
      boolean pendiente = resoluciones.size() != request.renglones().size();
      for (int i = 0; i < resoluciones.size(); i++) {
        var resolution = resoluciones.get(i);
        String selected = request.renglones().get(i).codigoSeleccionado();
        var codigos = new LinkedHashSet<String>();
        String origen;
        if (selected != null) {
          origen = "seleccion_explicita";
          long matches = resolution.candidatos().stream().filter(c -> selected.equals(c.codigoGP())).count();
          if (matches == 1) codigos.add(selected);
        } else if ("identificado".equals(resolution.estado()) && resolution.busquedaCompleta()
            && !resolution.busquedaTruncada() && resolution.datosFaltantes().isEmpty()
            && resolution.articulo() != null) {
          origen = "identificado";
          codigos.add(resolution.articulo().codigoGP());
        } else {
          origen = "opciones";
          // User-requested workflow: each returned option becomes an ordinary draft line for later review.
          for (var candidate : resolution.candidatos()) {
            if (!codigos.add(candidate.codigoGP()))
              advertencias.add("codigo_repetido_en_renglon_" + i + "_unificado_" + candidate.codigoGP());
          }
          advertencias.add("opciones_son_lineas_ordinarias_revisar_antes_de_cotizar");
        }
        boolean listaCompleta = resolution.busquedaCompleta() && !resolution.busquedaTruncada()
            && !resolution.candidatosTruncados();
        if (!resolution.busquedaCompleta() || resolution.busquedaTruncada())
          advertencias.add("busqueda_incompleta_renglon_" + i);
        if (resolution.candidatosTruncados()) advertencias.add("lista_candidatos_recortada_renglon_" + i);
        if (codigos.removeIf(code -> code == null || code.isBlank() || code.length() > 30)) {
          listaCompleta = false;
          advertencias.add("candidatos_sin_codigo_utilizable_renglon_" + i);
        }
        if (codigos.isEmpty()) {
          pendiente = true;
          advertencias.add("sin_opciones_utilizables_renglon_" + i);
        }
        for (String code : codigos) {
          var item = new Item(code, resolution.cantidad());
          var previous = items.putIfAbsent(code, item);
          if (previous != null) {
            if (previous.cantidad().compareTo(item.cantidad()) != 0) {
              pendiente = true;
              advertencias.add("codigo_con_cantidades_conflictivas_" + code);
            } else advertencias.add("codigo_repetido_misma_cantidad_unificado_" + code);
          }
        }
        opciones.add(new OpcionRenglon(i, List.copyOf(codigos), resolution.cantidad(), origen, listaCompleta));
      }
      if (items.size() > 20) {
        pendiente = true;
        advertencias.add("limite_20_lineas_superado_no_se_envia_lote_parcial");
      }
      if (pendiente) return new Resultado(422, resoluciones, null, false, List.copyOf(opciones), List.copyOf(advertencias));
      // No partial write; preserve the request quantity on EACH option. No prices or group metadata go upstream.
      enviado = true;
      var draft = client.crear(new Peticion(cliente, pv, List.copyOf(items.values())));
      var result = new Resultado(201, List.copyOf(resoluciones), draft, false, List.copyOf(opciones), List.copyOf(advertencias));
      intento.result = result;
      return result;
    } finally {
      if (!enviado) synchronized (intentos) { intentos.remove(id, intento); }
      // Once dispatch starts, even exceptions retain the UUID and block any second send in this process.
    }
  }

  private String fingerprint(Object value) {
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(mapper.writeValueAsBytes(value)));
    } catch (Exception exception) { throw new IllegalStateException(); }
  }

  public record OpcionRenglon(int indice, List<String> codigos, java.math.BigDecimal cantidad,
      String origen, boolean listaCompleta) {}
  public record Resultado(int status, List<ResolucionArticulosReporteDto> resoluciones,
      ResolucionBorradorDto borrador, boolean repeticionLocal, List<OpcionRenglon> opcionesPorRenglon,
      List<String> advertencias) {}
}