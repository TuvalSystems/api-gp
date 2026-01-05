package tuval.api.web.controller.transaccciones;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import tuval.api.web.models.Utils.ResponseJsonUtils;
import tuval.api.web.models.entity.tuval.transacciones.facturacionVenta.responseInvoiceVtaEntity;
import tuval.api.web.models.entity.tuval.transacciones.facturacionVenta.validateGenerateInvoiceResponseEntity;
import tuval.api.web.models.entity.tuval.transacciones.reciboCompraL.validateReceivedLResponseEntity;
import tuval.api.web.models.entity.tuval.transacciones.reciboCompraL.validateResponseEntity;
import tuval.api.web.models.request.transacciones.facturacionVenta.ReceivedInvoiceRequest;
import tuval.api.web.models.request.transacciones.facturacionVenta.ResponseConsultInvoiceRequest;
import tuval.api.web.models.request.transacciones.reciboCompraL.ReceivedSalesLRequest;
import tuval.api.web.models.request.transacciones.reciboCompraL.validateReceidLRequest;
import tuval.api.web.models.service.tuval.transacciones.transactionService;

@RestController
@RequestMapping("/api/transacciones")
public class TransactionController {

    @Autowired
    private transactionService transactionService;

    @CrossOrigin
    @PostMapping("/validar-generacion-documento-recibo-compra-l")
    public ResponseEntity<Map<String, Object>> validarGeneracionRecepcion(
            @RequestBody validateReceidLRequest request) {
        try {
            validateResponseEntity resultado = transactionService.validateReceivedL(request);
            return ResponseJsonUtils.buildSuccessWithData("Validacion exitosa", resultado);
        } catch (Exception e) {
            e.printStackTrace(); // para ver la traza completa
            return ResponseJsonUtils.buildException(e);
        }

    }

    @CrossOrigin
    @PostMapping("/eliminar-documento-recibo-compra-l")
    public ResponseEntity<Map<String, Object>> eliminarReciboItemL(@RequestBody validateReceidLRequest request) {
        try {
            validateResponseEntity resultado = transactionService.deleteReceivedL(request);
            return ResponseJsonUtils.buildSuccessWithData("Eliminacion exitosa", resultado);
        } catch (Exception e) {
            e.printStackTrace(); // para ver la traza completa
            return ResponseJsonUtils.buildException(e);
        }

    }

    @CrossOrigin
    @PostMapping("/guardar-documento-recibo-compra-l")
    public ResponseEntity<Map<String, Object>> guardarReciboCompraL(@Valid @RequestBody ReceivedSalesLRequest request) {
        try {
            validateReceivedLResponseEntity resultado = transactionService.storeReceivedL(request);
            return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", resultado);
        } catch (Exception e) {
            e.printStackTrace(); // para ver la traza completa
            return ResponseJsonUtils.buildException(e);
        }
    }

    @CrossOrigin
    @PostMapping("/generar-documento-recibo-compra-l")
    public ResponseEntity<Map<String, Object>> procesarReciboCompraL(
            @RequestBody validateReceidLRequest request) {
        try {
            validateReceivedLResponseEntity resultado = transactionService.processReceivedL(request);
            return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", resultado);
        } catch (Exception e) {
            e.printStackTrace(); // para ver la traza completa
            return ResponseJsonUtils.buildException(e);
        }
    }

    @CrossOrigin
    @PostMapping("/respuesta-documento-recibo-compra-l")
    public ResponseEntity<Map<String, Object>> respuestaReciboCompraL(
            @RequestBody validateReceidLRequest request) {
        try {
          validateResponseEntity resultado = transactionService.responseReceivedL(request);
            return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", resultado);
        } catch (Exception e) {
            e.printStackTrace(); // para ver la traza completa
            return ResponseJsonUtils.buildException(e);
        }
    }


    @CrossOrigin
    @PostMapping("/estado-recibo-compra-l")
    public ResponseEntity<Map<String, Object>> estadoReciboCompraL(@RequestBody validateReceidLRequest request){
        try {
            // validateResponseEntity resultado = transactionService.stateReceivedLService();
            return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", request);
        } catch (Exception e) {
            e.printStackTrace(); // para ver la traza completa
            return ResponseJsonUtils.buildException(e);
        }
    }

    /** proceso de generacion de factura - apis  */

    @CrossOrigin
    @PostMapping("/guardar-documento-factura-vta")
    public ResponseEntity<Map<String, Object>> guardarFacturaVenta(@Valid @RequestBody ReceivedInvoiceRequest request) {
       try {
        validateGenerateInvoiceResponseEntity resultado = transactionService.storeInvoice(request);
        return ResponseJsonUtils.buildSuccess(resultado.getMensaje());
            // return ResponseJsonUtils.buildSuccessWithData("Validacion exitosa", request);

       } catch (Exception e) {
         e.printStackTrace(); // para ver la traza completa
            return ResponseJsonUtils.buildException(e);
       }
    }
    
    @CrossOrigin
    @PostMapping("/respuesta-documento-factura-vta")
    public ResponseEntity<Map<String, Object>> validarGeneracionFacturaVenta(
            @RequestBody ResponseConsultInvoiceRequest request) {
        try {
            responseInvoiceVtaEntity resultado = transactionService.consultInvoiceVta(request);
            return ResponseJsonUtils.buildSuccessWithData("Validacion exitosa", resultado);
        } catch (Exception e) {
            e.printStackTrace(); // para ver la traza completa
            return ResponseJsonUtils.buildException(e);
        }

    }

    @CrossOrigin
    @PostMapping("/eliminar-documento-factura-vta")
    public ResponseEntity<Map<String, Object>> eliminarFacturaNoProcesadaVta(
            @RequestBody ResponseConsultInvoiceRequest request) {
        try {
            validateGenerateInvoiceResponseEntity resultado = transactionService.deleteInvoiceVta(request);
            return ResponseJsonUtils.buildSuccessWithData("Validacion exitosa", resultado);
        } catch (Exception e) {
            e.printStackTrace(); // para ver la traza completa
            return ResponseJsonUtils.buildException(e);
        }

    }


    
}
