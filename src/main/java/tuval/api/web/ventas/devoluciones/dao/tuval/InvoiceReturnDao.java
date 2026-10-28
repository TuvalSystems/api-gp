package tuval.api.web.ventas.devoluciones.dao.tuval;

import java.util.Map;
import java.util.List;
import org.springframework.http.ResponseEntity;
import tuval.api.web.ventas.devoluciones.dto.ItemBinResponseDto;
import tuval.api.web.ventas.devoluciones.dto.notacredito.DocumentNcDto;
import tuval.api.web.ventas.devoluciones.entity.tuval.DevolutionResponse;
import tuval.api.web.ventas.devoluciones.request.DetailDevolutionRequest;
import tuval.api.web.ventas.devoluciones.request.HeaderDevolutionRequest;
import tuval.api.web.ventas.devoluciones.request.HeaderNoteCreditRequest;

public interface InvoiceReturnDao {

  ResponseEntity<Map<String, Object>> ValidateGenerationDocumentVTANC(String document, String tipo);

  ResponseEntity<Map<String, Object>> eliminacionNcCabDet(String document, String tipo);

  ResponseEntity<Map<String, Object>> IncidenciaNcVta(
      String document, String tipo, String codigo, String message);

  ResponseEntity<Map<String, Object>> procesarCabeceraNotaCredito(
      String docu,
      String idTipo,
      String puntoVenta,
      String docId,
      String cliente,
      String vendedor,
      String direccion,
      String condPago,
      String notaDocu,
      String fecha,
      String bodega,
      double subtotal,
      double descuentoComercial,
      double impuesto,
      double total);

  ResponseEntity<Map<String, Object>> procesarDetalleNotaCredito(
      String docu,
      String tipo,
      double linea,
      String articulo,
      String descripcionArticulo,
      String unidad,
      String bodega,
      double cantidad,
      double costo,
      double precio,
      double descuento,
      double impuesto,
      double total);

  ResponseEntity<Map<String, Object>> guardarIncidenciaFactura(
      String docu, String tipo, String codigo, String descripcion);

  ResponseEntity<Map<String, Object>> obtenerTipoProducto(String articulo);

  ResponseEntity<Map<String, Object>> generarNotaCredito(String docu, String tipo);

  ResponseEntity<Map<String, Object>> respuestaNotaCredito(String docu, String tipo);

  public DevolutionResponse executeSpStoreHeaderDevolution(HeaderDevolutionRequest req, int modo);

  public DevolutionResponse executeSpStoreDetaillDevolution(
      DetailDevolutionRequest det, HeaderDevolutionRequest cab, int idmov, int secitem, int modo);

  public DevolutionResponse executeSpGenerateMovimentDevolution(
      HeaderDevolutionRequest req, int idmov, int modo);

  public ItemBinResponseDto consultItemBin(String item, String bodega, int modo);

  List<String> getClientGroupEconomic(String ruc);

  /** pruebas para generacion de nota de credito */
  DocumentNcDto executeSpExistenceNoteCreditDao(String document, String tipo);

  DocumentNcDto executeSpValidateExistsNc(HeaderNoteCreditRequest header);

  void executeSpDeleteExistenceNc(HeaderNoteCreditRequest header);
}
