package tuval.api.web.reportes.cotizacionborrador.controller;

import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.*;
import tuval.api.web.common.response.ResponseJsonUtils;
import tuval.api.web.reportes.cotizacionborrador.request.CotizacionBorradorReporteRequest;
import tuval.api.web.reportes.cotizacionborrador.service.tuvalbackend.CotizacionBorradorException;
import tuval.api.web.reportes.cotizacionborrador.service.tuvalbackend.CotizacionBorradorReporteService;

@RestController
@RequestMapping(value = "/api/reportes/cotizacion-borrador", produces = "application/json")
public class CotizacionBorradorReporteController {
  private final CotizacionBorradorReporteService service;
  public CotizacionBorradorReporteController(CotizacionBorradorReporteService service) { this.service = service; }

  @PostMapping(consumes = "application/json")
  public ResponseEntity<Map<String, Object>> crear(@Valid @RequestBody CotizacionBorradorReporteRequest request,
      @RequestHeader HttpHeaders headers) {
    try {
      var result = service.crear(request, headers.get(HttpHeaders.AUTHORIZATION));
      return ResponseEntity.status(result.status()).header(HttpHeaders.CACHE_CONTROL, "no-store")
          .body(ResponseJsonUtils.buildSuccessWithData("Cotizacion borrador disponible para revision.", result.data()).getBody());
    } catch (IllegalArgumentException exception) {
      return invalidRequest();
    } catch (CotizacionBorradorException exception) {
      int status = exception.status();
      String message = switch (status) {
        case 401 -> "Se requiere un Bearer de Laravel valido.";
        case 403 -> "El usuario no tiene permiso para crear este borrador.";
        case 409 -> "Conflicto de request_id o concurrencia. Compruebe el contenido antes de reintentar.";
        case 422 -> "Laravel rechazo los datos o la configuracion del borrador.";
        case 429 -> "Laravel limito la frecuencia de solicitudes.";
        case 503 -> "El puente de cotizacion borrador no esta habilitado o configurado.";
        default -> "No se pudo confirmar el resultado en Laravel. Reintente con el mismo request_id y contenido.";
      };
      var response = ResponseEntity.status(status).header(HttpHeaders.CACHE_CONTROL, "no-store");
      if (status == 401) response.header(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
      return response.body(Map.of("success", false, "message", message));
    } catch (Exception exception) {
      return ResponseJsonUtils.buildError("No se pudo procesar el borrador. Conserve el mismo request_id y contenido.");
    }
  }

  @ExceptionHandler({BindException.class, HttpMessageNotReadableException.class})
  public ResponseEntity<Map<String, Object>> invalidRequest() {
    return ResponseJsonUtils.buildNoFound("Parametros invalidos de cotizacion borrador.");
  }
}
