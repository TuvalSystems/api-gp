package tuval.api.web.models.service.tuval.devoluciones;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import tuval.api.web.models.entity.tuval.devoluciones.ItemsDevolucionEntity;
import tuval.api.web.models.entity.tuval.devoluciones.RetornoEntity;

public interface InvoiceReturnService {

    ResponseEntity<Map<String, Object>> generacionNotaCreditoVTA( Map<String, Object>  cabecera, List<Map<String, Object>>  detalle);

    ResponseEntity<Map<String, Object>> getResponseCreditNote(String document, String tipo);

     List<RetornoEntity> generateSalesReturn(String idCompania,
            String documento,
            String pedidoOrigen,
            String bodega,
            String observacion,
            String usuario,
            List<ItemsDevolucionEntity> itemsDevolucion);
} 
