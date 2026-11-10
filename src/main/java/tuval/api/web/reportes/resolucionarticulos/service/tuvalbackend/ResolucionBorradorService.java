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
        return new Resultado(200, result.resoluciones(), result.borrador(), true);
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
      var items = new ArrayList<Item>();
      var codigos = new HashSet<String>();
      boolean pendiente = resoluciones.size() != request.renglones().size();
      for (int i = 0; i < resoluciones.size(); i++) {
        var resolution = resoluciones.get(i);
        String selected = request.renglones().get(i).codigoSeleccionado();
        String codigo = null;
        if (selected != null) {
          // A manual choice must be among the current candidates, not an arbitrary client-supplied code.
          long matches = resolution.candidatos().stream().filter(c -> selected.equals(c.codigoGP())).count();
          if (matches == 1) codigo = selected;
        } else if ("identificado".equals(resolution.estado()) && resolution.busquedaCompleta()
            && !resolution.busquedaTruncada() && resolution.datosFaltantes().isEmpty()
            && resolution.articulo() != null) {
          codigo = resolution.articulo().codigoGP();
        }
        if (codigo == null || codigo.isBlank() || codigo.length() > 30 || !codigos.add(codigo)) pendiente = true;
        else items.add(new Item(codigo, resolution.cantidad()));
      }
      if (pendiente) return new Resultado(422, resoluciones, null, false);
      // No partial draft; no price, unit, actor or resolution metadata is sent to Laravel.
      enviado = true;
      var draft = client.crear(new Peticion(cliente, pv, List.copyOf(items)));
      var result = new Resultado(201, List.copyOf(resoluciones), draft, false);
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

  public record Resultado(int status, List<ResolucionArticulosReporteDto> resoluciones,
      ResolucionBorradorDto borrador, boolean repeticionLocal) {}
}