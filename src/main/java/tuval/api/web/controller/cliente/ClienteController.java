package tuval.api.web.controller.cliente;

import jakarta.validation.Valid;
import java.util.List;
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
import tuval.api.web.models.dto.cliente.ListAllSegmentsDto;
import tuval.api.web.models.dto.cliente.ListAllSubsegmentsDto;
import tuval.api.web.models.dto.cliente.ListSegmentClientDto;
import tuval.api.web.models.dto.cliente.ListSellerDto;
import tuval.api.web.models.dto.cliente.NewAddressStatusDto;
import tuval.api.web.models.dto.cliente.TypeSellerDto;
import tuval.api.web.models.request.cliente.ClientRequest;
import tuval.api.web.models.request.cliente.ClientSubsegmentRucRequest;
import tuval.api.web.models.request.cliente.NewAddressRequest;
import tuval.api.web.models.request.cliente.SellerRequest;
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
      ListSellerDto response = clienteService.getListSeller(req);
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", response);

    } catch (Exception e) {
      e.printStackTrace();
      return ResponseJsonUtils.buildException(e);
    }
  }

  @CrossOrigin
  @GetMapping("/obtener-segmentos")
  public ResponseEntity<Map<String, Object>> obtenerTodosLosSegmentos() {
    try {
      List<ListAllSegmentsDto> response = clienteService.getAllSegmentos();
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", response);
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseJsonUtils.buildException(e);
    }
  }

  @CrossOrigin
  @GetMapping("/obtener-subsegmento-segmento")
  public ResponseEntity<Map<String, Object>> obtenerSubSegmentosPorSegmento(
      @RequestParam int coAtrPadre) {
    try {
      List<ListAllSubsegmentsDto> response = clienteService.getAllSubSegmentsBySegment(coAtrPadre);
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
      ListSegmentClientDto response = clienteService.getListSegmentos(ruc);
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
      TypeSellerDto response = clienteService.getTypeSellerForUserService(username);
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", response);
    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }

  @CrossOrigin
  @PostMapping("/crear-modificar-cliente")
  public ResponseEntity<Map<String, Object>> postMethodName(@RequestBody ClientRequest request) {
    try {
      clienteService.storeClienteService(request);
      return ResponseJsonUtils.buildSuccess("Cliente guardado exitosamente");
    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }

  @CrossOrigin
  @PostMapping("/crear-registro-subsegmento-ruc")
  public ResponseEntity<Map<String, Object>> postMethodName(
      @RequestBody ClientSubsegmentRucRequest req) {
    try {
      clienteService.storeClientSubSegmentAndRucService(req);
      return ResponseJsonUtils.buildSuccess("Cliente guardado exitosamente");
    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }


  @CrossOrigin
  @PostMapping("crear-nueva-direccion")
  public ResponseEntity<Map<String, Object>> crearNuevaDireccionCliente(@RequestBody NewAddressRequest request) {
    try {
      NewAddressStatusDto respString = clienteService.storeNewAddressService(request);
      return ResponseJsonUtils.buildSuccessWithData("Respuesta Exitosa", respString);
    } catch (Exception e) {
        return ResponseJsonUtils.buildException(e);
    }
    
  }
}
