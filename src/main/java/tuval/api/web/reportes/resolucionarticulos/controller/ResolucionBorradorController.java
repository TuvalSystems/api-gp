package tuval.api.web.reportes.resolucionarticulos.controller;

import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.*;
import tuval.api.web.reportes.cotizacionborrador.service.tuvalbackend.CotizacionBorradorException;
import tuval.api.web.reportes.resolucionarticulos.request.ResolucionBorradorRequest;
import tuval.api.web.reportes.resolucionarticulos.service.tuvalbackend.ResolucionBorradorService;

@RestController
@RequestMapping(value = "/api/reportes/resolucion-articulos/crear-borrador", produces = "application/json")
public class ResolucionBorradorController {
  private final ResolucionBorradorService service;
  public ResolucionBorradorController(ResolucionBorradorService service) { this.service = service; }

  @PostMapping(consumes = "application/json")
  public ResponseEntity<Map<String, Object>> crear(@Valid @RequestBody ResolucionBorradorRequest request) {
    try {
      var result = service.crear(request);
      if (result.status() == 422) return response(422, Map.of("success", false,
          "message", "Revise los renglones: faltan selecciones explicitas, hay codigos repetidos o no se pudo resolver todo el lote.",
          "data", Map.of("request_id", request.requestId(), "resoluciones", result.resoluciones(), "creado", false)));
      return response(result.status(), Map.of("success", true,
          "message", "Borrador de prueba disponible para revision. Los precios se validan al generar la cotizacion.",
          "data", Map.of("request_id", request.requestId(), "borrador", result.borrador(),
              "resoluciones", result.resoluciones(), "repeticion_local", result.repeticionLocal())));
    } catch (IllegalArgumentException exception) {
      return invalidRequest();
    } catch (CotizacionBorradorException exception) {
      int status = exception.status();
      String message = switch (status) {
        case 401, 403 -> "La API de pruebas rechazo la autorizacion de la operacion.";
        case 422 -> "La API de pruebas rechazo los articulos o la configuracion del borrador.";
        case 429 -> "La API de pruebas limito las solicitudes.";
        case 409 -> "La solicitud ya esta en curso, tiene otro contenido o su resultado es incierto. Revise Laravel antes de intentar nuevamente.";
        case 503 -> "La integracion esta deshabilitada, su destino no es valido o el registro local esta lleno.";
        default -> "No se pudo confirmar la creacion. Revise la cotizacion en Laravel antes de intentar nuevamente.";
      };
      return response(status, Map.of("success", false, "message", message, "reintentable", false));
    } catch (Exception exception) {
      return response(500, Map.of("success", false,
          "message", "No se pudo completar la operacion. Revise Laravel antes de intentar nuevamente.", "reintentable", false));
    }
  }

  @ExceptionHandler({BindException.class, HttpMessageNotReadableException.class})
  public ResponseEntity<Map<String, Object>> invalidRequest() {
    return response(400, Map.of("success", false, "message", "Parametros invalidos de creacion de borrador."));
  }
  private static ResponseEntity<Map<String, Object>> response(int status, Map<String, Object> body) {
    return ResponseEntity.status(status).header(HttpHeaders.CACHE_CONTROL, "no-store").body(body);
  }
}