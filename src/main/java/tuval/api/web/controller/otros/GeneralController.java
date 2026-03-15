package tuval.api.web.controller.otros;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import tuval.api.web.models.Utils.ResponseJsonUtils;
import tuval.api.web.models.dto.otros.pedidos.OrderDetailDto;
import tuval.api.web.models.dto.otros.pedidos.OrderHeaderDto;
import tuval.api.web.models.request.otros.pedidos.OrdersDetailRequest;
import tuval.api.web.models.request.otros.pedidos.OrdersHeadersRequest;
import tuval.api.web.models.service.tuval.otros.GeneralService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping(value = "/api", produces = "application/json")
@RequiredArgsConstructor
public class GeneralController {

    /** estas api son para actualizar datos historicos de facturas */
    private final GeneralService generalService;

    @CrossOrigin
    @GetMapping("facturas-cabecera-pendientes")
    public ResponseEntity<Map<String, Object>> obtenerListaCabeceraPedidosPendientes(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SS") LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SS") LocalDateTime fechaFin) {
        try {
            List<OrderHeaderDto> response = generalService.getAllOrderPendingUpdate(fechaInicio, fechaFin);
            return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", response);
        } catch (Exception e) {
            return ResponseJsonUtils.buildException(e);
        }
    }

    @CrossOrigin
    @PostMapping("facturas-detalles-pendientes")
    public ResponseEntity<Map<String, Object>> obtenerListaDetallePedidosPendientes(
            @RequestBody Map<String, List<String>> payload) {
        try {
            List<OrderDetailDto> response = generalService.getAllOrderDetailsPendingUpdate(payload);
            return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", response);
        } catch (Exception e) {
            return ResponseJsonUtils.buildException(e);
        }
    }

    @PostMapping("update-headers")
    public ResponseEntity<Map<String, Object>> updateHeaders(@RequestBody List<OrdersHeadersRequest> payload) {
        try {
            var result = generalService.processMassiveHeaderUpdate(payload);
            return ResponseJsonUtils.buildSuccessWithData("Actualización de cabeceras exitosa", result);
        } catch (Exception e) {
            e.printStackTrace(); // Log
            return ResponseJsonUtils.buildException(e);
        }
    }

    // Endpoint para DETALLES
    @PostMapping("update-details")
    public ResponseEntity<Map<String, Object>> updateDetails(@RequestBody List<OrdersDetailRequest> payload) {
        try {
            var result = generalService.processMassiveDetailUpdate(payload);
            return ResponseJsonUtils.buildSuccessWithData("Actualización de detalles exitosa", result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseJsonUtils.buildException(e);
        }
    }

}
