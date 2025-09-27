package tuval.api.web.controller.reservas;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tuval.api.web.models.Utils.ResponseJsonUtils;
import tuval.api.web.models.entity.dynamics.reservas.NotifyLocalEntity;
import tuval.api.web.models.service.dynamics.reservas.ReservedService;

@RestController
@RequestMapping("/api/reserva")
public class ReservedController {

  @Autowired private ReservedService reservedService;

  @CrossOrigin
  @GetMapping("/obtener-notificaciones-local")
  public ResponseEntity<Map<String, Object>> obtenerNotificacionesLocal(
      @RequestParam("ptovta") String ptovta, @RequestParam("conot") int conot) {
    try {

      NotifyLocalEntity data = reservedService.findByTxPtoVta(ptovta, conot);
      return ResponseJsonUtils.buildSuccessWithData("Respuesta obtenida correctamente", data);

    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }
}
