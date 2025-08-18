package tuval.api.web.controller.transaccciones;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import tuval.api.web.models.Utils.ResponseJsonUtils;
import tuval.api.web.models.dto.transacciones.reciboCompraL.ReceivedSalesLRequest;
import tuval.api.web.models.dto.transacciones.reciboCompraL.validateReceidLRequestEntity;
import tuval.api.web.models.entity.tuval.transacciones.reciboCompraL.validateResponseEntity;
import tuval.api.web.models.service.tuval.transacciones.transactionService;




@RestController
@RequestMapping("/api/transacciones")
public class TransactionController {

    @Autowired
    private transactionService transactionService;

    @CrossOrigin
    @RequestMapping("/validar-generacion-recepcion")
    public ResponseEntity<Map<String, Object>> validarGeneracionRecepcion(@RequestBody validateReceidLRequestEntity request) {
        try {
            validateResponseEntity resultado = transactionService.validateReceivedL(request);
            return ResponseJsonUtils.buildSuccessWithData("Validacion exitosa", resultado);
        } catch (Exception e) {
            e.printStackTrace();  // para ver la traza completa
            return ResponseJsonUtils.buildException(e);
        }

    }

    @CrossOrigin
    @RequestMapping("/eliminar-recibo-compra")
     public ResponseEntity<Map<String, Object>> eliminarReciboItemL(@RequestBody validateReceidLRequestEntity request) {
       try {
            validateResponseEntity resultado = transactionService.deleteReceivedL(request);
            return ResponseJsonUtils.buildSuccessWithData("Eliminacion exitosa", resultado);
        } catch (Exception e) {
            e.printStackTrace();  // para ver la traza completa
            return ResponseJsonUtils.buildException(e);
        }

    }


    @CrossOrigin
    @RequestMapping("/guardar-recibo-compra-l")
    public ResponseEntity<Map<String, Object>> requestMethodName(@Valid @RequestBody ReceivedSalesLRequest request) {
       try {
            // validateResponseEntity resultado = transactionService.deleteReceivedL(request);
            return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", request);
        } catch (Exception e) {
            e.printStackTrace();  // para ver la traza completa
            return ResponseJsonUtils.buildException(e);
        }
    }
    
    
}
