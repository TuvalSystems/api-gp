package tuval.api.web.controller.guiaremision;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tuval.api.web.models.Utils.ResponseJsonUtils;
import tuval.api.web.models.entity.tuval.guiaremision.CheckCodeAccessDeliveryNoteResponse;
import tuval.api.web.models.entity.tuval.guiaremision.DetailCheckGuideAnyResponse;
import tuval.api.web.models.entity.tuval.guiaremision.HeaderCheckGuideDispatchResponse;
import tuval.api.web.models.entity.tuval.guiaremision.HeaderGuideTransferVtaResponse;
import tuval.api.web.models.entity.tuval.guiaremision.ValidateExistsDeliveryNoteResponse;
import tuval.api.web.models.request.bodegas.guiaremision.DispatchDeliveryNoteMovementRequest;
import tuval.api.web.models.request.bodegas.guiaremision.DispatchVtaRequest;
import tuval.api.web.models.request.bodegas.guiaremision.TransferVtaRequest;
import tuval.api.web.models.request.bodegas.guiaremision.ValidateGuideDispatchRequest;
import tuval.api.web.models.request.bodegas.guiaremision.ValidateGuideSalesTransferedRequest;
import tuval.api.web.models.request.bodegas.guiaremision.crearGuia.DeliveryNoteRequest;
import tuval.api.web.models.service.tuval.guiaremision.ShippingGuideService;

@RestController
@RequestMapping(value = "/api", produces = "application/json")
@RequiredArgsConstructor
public class ShippingGuideController {

  private final ShippingGuideService shippingGuideService;

  /***
   * PROCESO DE CREACION DE GUIAS DE REMISION GENERAL
   * 
   */

  @CrossOrigin
  @PostMapping("/inventario/crear-guia-remision")
  public ResponseEntity<Map<String, Object>> crearGuiaRemision(
      @Valid @RequestBody DeliveryNoteRequest req) {
    try {
      shippingGuideService.createDeliveryNoteService(req);
      return ResponseJsonUtils.buildSuccess("Guia de Remisión creada con exito");
    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }

  @CrossOrigin
  @GetMapping("/inventario/validar-existencia-guia-remision")
  public ResponseEntity<Map<String, Object>> validarExistenciaGuiaRemision(
      @Valid @RequestParam String numDoc) {
    try {
      ValidateExistsDeliveryNoteResponse resp = shippingGuideService.checkExistenceDeliveryNoteService(numDoc);
      return ResponseJsonUtils.buildSuccessWithData("Validacion exitosa", resp);
    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }

  @CrossOrigin
  @GetMapping("/inventario/guia-remision-obtener-claveacceso")
  public ResponseEntity<Map<String, Object>> respuestaGuiaRemision(@RequestParam String numDoc) {
    if (numDoc == null || numDoc.trim().isEmpty()) {
      return ResponseJsonUtils.buildError("El documento es requerido.");
    }
    try {
      CheckCodeAccessDeliveryNoteResponse result = shippingGuideService.checkGuiaRemisionService(numDoc);
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", result);
    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }

  /**
   * 
   * GUIA DE TRASLADO DE VENTA
   */

  // DESPACHO DE GUIA DE TRANSFERENCUIA DE VENTA
  @CrossOrigin
  @PostMapping("/inventario/guardar-generar-movimiento-transferencia-venta")
  public ResponseEntity<Map<String, Object>> generarTranasferenciaVenta(
      @Valid @RequestBody TransferVtaRequest req) {
    try {
      HeaderGuideTransferVtaResponse response = shippingGuideService.generateGuideTransferVtaService(req);
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", response);

    } catch (Exception e) {
      // e.printStackTrace(); // para ver la traza completa
      return ResponseJsonUtils.buildException(e);
    }
  }

  @CrossOrigin
  @GetMapping("/inventario/validar-existencia-guia-transferencia")
  public ResponseEntity<Map<String, Object>> obtenerMovimientoGuiaTransferencia(
      @Valid @RequestParam String orderNumber,
      @RequestParam(required = false) String newOrderNumber,
      @RequestParam(required = false) String origen,
      @Valid @RequestParam Integer idGuiaRemision,
      @Valid @RequestParam String guiaRemision) {

    try {
      HeaderCheckGuideDispatchResponse resp = shippingGuideService.checkAjusteGuideTransferVtaService(
          orderNumber, newOrderNumber, origen, idGuiaRemision, guiaRemision);

      if (resp == null) {
        return ResponseJsonUtils.buildNoFound("No existe guía para mc_numdoc=" + orderNumber);
      }
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", resp);

    } catch (Exception e) {
      // e.printStackTrace(); // para ver la traza completa
      return ResponseJsonUtils.buildException(e);
    }
  }

  // RECIBIR GUIAS DE TRANSFERENCIAS DE VENTAS
  @CrossOrigin
  @PostMapping("inventario/transferencia-ventas/recibir/guardar-generar-movimiento")
  public ResponseEntity<Map<String, Object>> recibirTransferenciaVenta(@Valid @RequestBody TransferVtaRequest req) {
    try {
      HeaderGuideTransferVtaResponse response = shippingGuideService.receivedGuideSalesTransferService(req);
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", response);

    } catch (Exception e) {
      // e.printStackTrace(); // para ver la traza completa
      return ResponseJsonUtils.buildException(e);
    }
  }

  // VALIDAR EXISTENCIA DE GUIAS DE TRANSFERENCIAS DE VENTAS - RECIBIDO
  @CrossOrigin
  @PostMapping("inventario/transferencia-ventas/validar-existencia-ajuste-recibir")
  public ResponseEntity<Map<String, Object>> obtenerMovimientoIngresoTransferenciaVenta(
      @Valid @RequestBody ValidateGuideSalesTransferedRequest request) {

    try {
      HeaderCheckGuideDispatchResponse resp = shippingGuideService.checkAjusteGuideSalesTransferReceivedService(
          request);
      if (resp == null) {
        return ResponseJsonUtils.buildNoFound(
            "No existe ajuste de recibido para el pedido: " + request.getOrderNumber());
      }
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", resp);

    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }

  /***
   * 
   * GUIA DE REPOSICION
   * 
   */

  @CrossOrigin
  @PostMapping("/inventario/guardar-generar-movimiento-reposicion")
  public ResponseEntity<Map<String, Object>> generarDepachoReposicion(
      @Valid @RequestBody TransferVtaRequest req) {
    try {
      HeaderGuideTransferVtaResponse response = shippingGuideService.generateGuideReposicionService(req);
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", response);

    } catch (Exception e) {
      // e.printStackTrace(); // para ver la traza completa
      return ResponseJsonUtils.buildException(e);
    }
  }

  @CrossOrigin
  @GetMapping("/inventario/validar-existencia-guia-reposicion")
  public ResponseEntity<Map<String, Object>> obtenerMovimientoGuiaReposicion(
      @Valid @RequestParam String orderNumber,
      @RequestParam(required = false) String origen,
      @RequestParam(required = false) Integer idMovimiento,
      @RequestParam(required = false) Integer idGuiaRemision,
      @RequestParam(required = false) String guiaRemision) {

    try {
      HeaderCheckGuideDispatchResponse resp = shippingGuideService.checkAjusteGuideRepositionVtaService(
          orderNumber, origen, idMovimiento, idGuiaRemision, guiaRemision);

      if (resp == null) {
        return ResponseJsonUtils.buildNoFound("No existe guía para mc_numdoc=" + orderNumber);
      }
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", resp);

    } catch (Exception e) {
      // e.printStackTrace(); // para ver la traza completa
      return ResponseJsonUtils.buildException(e);
    }
  }

  // RECIBIR GUIAS DE REPOSICION
  @CrossOrigin
  @PostMapping("inventario/reposiciones/recibir/guardar-generar-movimiento")
  public ResponseEntity<Map<String, Object>> recibirReposiciones(@Valid @RequestBody TransferVtaRequest req) {
    try {
      HeaderGuideTransferVtaResponse response = shippingGuideService.receivedGuideReplenishmentTransferService(req);
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", response);

    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }

  /***
   * 
   * GUIA DE DESPACHO
   * 
   * 
   */

  // DESPACHO DE GUIAS DE DESPACHO

  @CrossOrigin
  @PostMapping("/inventario/guardar-generar-movimiento-despacho")
  public ResponseEntity<Map<String, Object>> generarDespachoVta(
      @Valid @RequestBody DispatchVtaRequest req) {
    try {
      HeaderGuideTransferVtaResponse response = shippingGuideService.generateGuideDispatchVtaService(req);
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", response);

    } catch (Exception e) {
      // e.printStackTrace(); // para ver la traza completa
      return ResponseJsonUtils.buildException(e);
    }
  }

  // validar existencia de ajuste de guia de remision
  @CrossOrigin
  @PostMapping("/inventario/validar-existencia-guia-despacho")
  public ResponseEntity<Map<String, Object>> obtenerMovimientoGuiaDespacho(
      @Valid @RequestBody ValidateGuideDispatchRequest req) {

    try {
      HeaderCheckGuideDispatchResponse resp = shippingGuideService.checkAjusteGuideDispatchVtaService(
          req);

      if (resp == null) {
        return ResponseJsonUtils.buildNoFound("No existe guía para mc_numdoc=" + req.getOrderNumber());
      }
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", resp);

    } catch (Exception e) {
      // e.printStackTrace(); // para ver la traza completa
      return ResponseJsonUtils.buildException(e);
    }
  }

  @CrossOrigin
  @GetMapping("/inventario/validar-existencia-detalle-guia-ajuste")
  public ResponseEntity<Map<String, Object>> obtenerExistenciaDetalleGuia(
      @Valid @RequestParam String orderNumber,
      @RequestParam(required = false) String newOrderNumber,
      @Valid @RequestParam String item,
      @RequestParam(required = false) Integer idMovimiento,
      @RequestParam(required = false) Integer cantidad) {

    try {
      DetailCheckGuideAnyResponse resp = shippingGuideService.checkAjustmentDetailGuideService(
          orderNumber, newOrderNumber, item, idMovimiento, cantidad);

      if (resp == null) {
        return ResponseJsonUtils.buildNoFound(
            "No existe movimiento del detalle para el item= "
                + item
                + " del pedido= "
                + orderNumber
                + " y cantidad= "
                + cantidad);
      }
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", resp);

    } catch (Exception e) {
      // e.printStackTrace(); // para ver la traza completa
      return ResponseJsonUtils.buildException(e);
    }
  }

  /***
   * 
   *
   * 
   * GUIA DE RESERVAS
   * 
   * 
   */

  @CrossOrigin
  @PostMapping("/inventario/reservas/validar-existencia-guia")
  public ResponseEntity<Map<String, Object>> obtenerMovimientoGuiaReserva(
      @Valid @RequestBody ValidateGuideSalesTransferedRequest request) {

    try {
      HeaderCheckGuideDispatchResponse resp = shippingGuideService.checkAjusteGuideReservationService(
          request);
      if (resp == null) {
        return ResponseJsonUtils.buildNoFound("No existe guía para mc_numdoc=" + request.getOrderNumber());
      }
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", resp);
    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }

  @CrossOrigin
  @PostMapping("/inventario/reservas/guardar-generar-movimiento/transferencia")
  public ResponseEntity<Map<String, Object>> generarTransferenciaReserva(
      @Valid @RequestBody TransferVtaRequest req) {
    try {
      HeaderGuideTransferVtaResponse response = shippingGuideService.generateGuideReservationService(req);
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", response);

    } catch (Exception e) {
      // e.printStackTrace(); // para ver la traza completa
      return ResponseJsonUtils.buildException(e);
    }
  }

  /*** procesar recibir transferencias de reservas */

  @CrossOrigin
  @GetMapping("/inventario/reservas/validar-existencia-ajuste-recibir")
  public ResponseEntity<Map<String, Object>> obtenerMovimientoGuiaReservaRecibida(
      @Valid @RequestParam String orderNumber,
      @RequestParam(required = false) String newOrderNumber,
      @RequestParam(required = false) String origen,
      @Valid @RequestParam Integer idGuiaRemision,
      @Valid @RequestParam String guiaRemision) {

    try {
      HeaderCheckGuideDispatchResponse resp = shippingGuideService.checkAjusteGuideReservationReceivedService(
          orderNumber, newOrderNumber, origen, idGuiaRemision, guiaRemision);

      if (resp == null) {
        return ResponseJsonUtils.buildNoFound("No existe ajuste de recibido para mc_numdoc = " + orderNumber);
      }
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", resp);

    } catch (Exception e) {
      // e.printStackTrace(); // para ver la traza completa
      return ResponseJsonUtils.buildException(e);
    }
  }

  @CrossOrigin
  @PostMapping("/inventario/reservas/guardar-generar-movimiento/recibir")
  public ResponseEntity<Map<String, Object>> recibirTransferenciaReserva(
      @Valid @RequestBody TransferVtaRequest req) {
    try {
      HeaderGuideTransferVtaResponse response = shippingGuideService.receiveGuideReservationService(req);
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", response);

    } catch (Exception e) {
      // e.printStackTrace(); // para ver la traza completa
      return ResponseJsonUtils.buildException(e);
    }
  }

  /** FIX PARA GENERAR LOS MOVIMIENTOS DE GUIAS */

  @CrossOrigin
  @PostMapping("/inventario/despacho-generar-movimiento")
  public ResponseEntity<Map<String, Object>> generarMovimientoGuiaDespacho(
      @Valid @RequestBody DispatchDeliveryNoteMovementRequest req) {
    try {
      HeaderGuideTransferVtaResponse response = shippingGuideService.generateMovementDispatchGuide(req);
      return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", response);

    } catch (Exception e) {
      // e.printStackTrace(); // para ver la traza completa
      return ResponseJsonUtils.buildException(e);
    }
  }

}
