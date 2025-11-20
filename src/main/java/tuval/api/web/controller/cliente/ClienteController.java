package tuval.api.web.controller.cliente;

import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tuval.api.web.models.Utils.ResponseJsonUtils;
import tuval.api.web.models.dto.cliente.SellerRequest;
import tuval.api.web.models.entity.dynamics.cliente.ListSegmentClientEntity;
import tuval.api.web.models.entity.dynamics.cliente.ListSellerEntity;
import tuval.api.web.models.entity.dynamics.cliente.TypeSellerEntity;
import tuval.api.web.models.service.dynamics.cliente.ClientService;

@RestController
@RequestMapping(value = "/api/cliente", produces = "application/json")
public class ClienteController {

  @Autowired private ClientService clienteService;

  @CrossOrigin
  @PostMapping("/obtener-tipo-vendedor")
  public ResponseEntity<Map<String, Object>> getSellerListClient(
      @Valid @RequestBody SellerRequest req) {
    try {
      ListSellerEntity response = clienteService.getListSeller(req);
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", response);

    } catch (Exception e) {
      e.printStackTrace();
      return ResponseJsonUtils.buildException(e);
    }
  }

  @CrossOrigin
  @GetMapping("/obtener-segmento-cliente")
  public ResponseEntity<Map<String, Object>> getListSegmento(@Valid @RequestParam String ruc) {

    if (ruc.isEmpty()) {
      return ResponseJsonUtils.buildError("Falta el campo " + "ruc" + "o está vacío.");
    }
    try {
      ListSegmentClientEntity response = clienteService.getListSegmentos(ruc);
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", response);

    } catch (Exception e) {
      e.printStackTrace();
      return ResponseJsonUtils.buildException(e);
    }
  }

  @CrossOrigin
  @GetMapping("/obtener-tipo-venededor-detalle")
  public ResponseEntity<Map<String, Object>> getTipoVendedor(@RequestParam String username) {
    try {
       TypeSellerEntity response = clienteService.getTypeSellerForUserService(username);
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", response);
    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }
}
