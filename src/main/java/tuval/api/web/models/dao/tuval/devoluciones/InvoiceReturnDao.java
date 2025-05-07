package tuval.api.web.models.dao.tuval.devoluciones;

import java.util.Map;

import org.springframework.http.ResponseEntity;

public interface InvoiceReturnDao {

        ResponseEntity<Map<String, Object>> ValidateGenerationDocumentVTANC(String document, String tipo);

        ResponseEntity<Map<String, Object>> eliminacionNcCabDet(String document, String tipo);

        ResponseEntity<Map<String, Object>> IncidenciaNcVta(String document, String tipo, String codigo,
                        String message);

        ResponseEntity<Map<String, Object>> procesarCabeceraNotaCredito(String docu, String idTipo, String puntoVenta,
                        String docId, String cliente, String vendedor, String direccion, String condPago,
                        String notaDocu,
                        String fecha, String bodega, double subtotal, double descuentoComercial, double impuesto,
                        double total);

        ResponseEntity<Map<String, Object>> procesarDetalleNotaCredito(String docu, String tipo, double linea,
                        String articulo, String descripcionArticulo, String unidad, String bodega, double cantidad,
                        double costo,
                        double precio, double descuento, double impuesto, double total);

        ResponseEntity<Map<String, Object>> guardarIncidenciaFactura(String docu, String tipo, String codigo,
                        String descripcion);

        ResponseEntity<Map<String, Object>> obtenerTipoProducto(String articulo);

        ResponseEntity<Map<String, Object>> generarNotaCredito(String docu, String tipo);

        ResponseEntity<Map<String, Object>> respuestaNotaCredito(String docu, String tipo);

        ResponseEntity<Map<String, Object>> executeCabeceraDevolucionVenta(String pedido, String transaccionOriginal,
                        String bodegaOrigen, String observacion, String usuario, int modo);

        ResponseEntity<Map<String, Object>> executeDetalleTransferenciaVenta(
                        String pedido,
                        String item,
                        int secuencia,
                        String bodegaOrigen,
                        String bodegaDestino,
                        float cantidad,
                        int idMovimiento,
                        String transaccionOriginal,
                        String binItem,
                        String compartimentoOrigen,
                        String compartimentoDestino,
                        String usuario,
                        int modo);

        ResponseEntity<Map<String, Object>> generateTransferMovement(
                        String pedido,
                        String bodegaOrigen,
                        String bodegaDestino,
                        String observacion,
                        int idMovimiento,
                        String transaccionOriginal,
                        String usuario,
                        int modo);
}