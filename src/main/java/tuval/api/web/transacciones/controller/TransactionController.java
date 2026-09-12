package tuval.api.web.transacciones.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tuval.api.web.common.response.ResponseJsonUtils;
import tuval.api.web.ventas.facturacion.dto.responseInvoiceVtaDto;
import tuval.api.web.ventas.facturacion.dto.validateGenerateInvoiceDto;
import tuval.api.web.bodega.recepciones.dto.validateReceivedLDto;
import tuval.api.web.bodega.recepciones.dto.StatusReceptionOrderItemLDto;
import tuval.api.web.bodega.recepciones.dto.validateDto;
import tuval.api.web.ventas.facturacion.request.ReceivedInvoiceRequest;
import tuval.api.web.ventas.facturacion.request.ResponseConsultInvoiceRequest;
import tuval.api.web.bodega.recepciones.request.ReceivedSalesLRequest;
import tuval.api.web.bodega.recepciones.request.validateReceidLRequest;
import tuval.api.web.transacciones.service.tuval.transactionService;

@RestController
@RequestMapping("/api/transacciones")
@RequiredArgsConstructor

public class TransactionController {

    private final  transactionService transactionService;

    @CrossOrigin
    @PostMapping("/validar-generacion-documento-recibo-compra-l")
    public ResponseEntity<Map<String, Object>> validarGeneracionRecepcion(
            @RequestBody validateReceidLRequest request) {
        try {
            validateDto resultado = transactionService.validateReceivedL(request);
            return ResponseJsonUtils.buildSuccessWithData("Validacion exitosa", resultado);
        } catch (Exception e) {
            return ResponseJsonUtils.buildException(e);
        }

    }

    @CrossOrigin
    @PostMapping("/eliminar-documento-recibo-compra-l")
    public ResponseEntity<Map<String, Object>> eliminarReciboItemL(@RequestBody validateReceidLRequest request) {
        try {
            validateDto resultado = transactionService.deleteReceivedL(request);
            return ResponseJsonUtils.buildSuccessWithData("Eliminacion exitosa", resultado);
        } catch (Exception e) {
            return ResponseJsonUtils.buildException(e);
        }

    }

    @CrossOrigin
    @PostMapping("/guardar-documento-recibo-compra-l")
    public ResponseEntity<Map<String, Object>> guardarReciboCompraL(@Valid @RequestBody ReceivedSalesLRequest request) {
        try {
            validateReceivedLDto resultado = transactionService.storeReceivedL(request);
            return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", resultado);
        } catch (Exception e) {
            return ResponseJsonUtils.buildException(e);
        }
    }

    @CrossOrigin
    @PostMapping("/generar-documento-recibo-compra-l")
    public ResponseEntity<Map<String, Object>> procesarReciboCompraL(
            @RequestBody validateReceidLRequest request) {
        try {
            validateReceivedLDto resultado = transactionService.processReceivedL(request);
            return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", resultado);
        } catch (Exception e) {
            return ResponseJsonUtils.buildException(e);
        }
    }

    @CrossOrigin
    @PostMapping("/respuesta-documento-recibo-compra-l")
    public ResponseEntity<Map<String, Object>> respuestaReciboCompraL(
            @RequestBody validateReceidLRequest request) {
        try {
          validateDto resultado = transactionService.responseReceivedL(request);
            return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", resultado);
        } catch (Exception e) {
            return ResponseJsonUtils.buildException(e);
        }
    }


    @CrossOrigin
    @PostMapping("/estado-recepcion-recibo-compra-l")
    public ResponseEntity<Map<String, Object>> estadoReciboCompraL(@RequestBody validateReceidLRequest request){
        try {
            StatusReceptionOrderItemLDto resultado = transactionService.stateReceivedLService(request);
            return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", resultado);
        } catch (Exception e) {
            return ResponseJsonUtils.buildException(e);
        }
    }

    /** proceso de generacion de factura - apis  */

    @CrossOrigin
    @PostMapping("/guardar-documento-factura-vta")
    public ResponseEntity<Map<String, Object>> guardarFacturaVenta(@Valid @RequestBody ReceivedInvoiceRequest request) {
       try {
        validateGenerateInvoiceDto resultado = transactionService.storeInvoice(request);
        return ResponseJsonUtils.buildSuccess(resultado.getMensaje());

       } catch (Exception e) {
            return ResponseJsonUtils.buildException(e);
       }
    }
    
    @CrossOrigin
    @PostMapping("/respuesta-documento-factura-vta")
    public ResponseEntity<Map<String, Object>> validarGeneracionFacturaVenta(
            @RequestBody ResponseConsultInvoiceRequest request) {
        try {
            responseInvoiceVtaDto resultado = transactionService.consultInvoiceVta(request);
            return ResponseJsonUtils.buildSuccessWithData("Validacion exitosa", resultado);
        } catch (Exception e) {
            return ResponseJsonUtils.buildException(e);
        }

    }

    @CrossOrigin
    @PostMapping("/eliminar-documento-factura-vta")
    public ResponseEntity<Map<String, Object>> eliminarFacturaNoProcesadaVta(
            @RequestBody ResponseConsultInvoiceRequest request) {
        try {
            validateGenerateInvoiceDto resultado = transactionService.deleteInvoiceVta(request);
            return ResponseJsonUtils.buildSuccessWithData("Validacion exitosa", resultado);
        } catch (Exception e) {
            return ResponseJsonUtils.buildException(e);
        }

    }


    
}
