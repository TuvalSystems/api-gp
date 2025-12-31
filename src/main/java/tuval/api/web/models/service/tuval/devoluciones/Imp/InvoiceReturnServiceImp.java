package tuval.api.web.models.service.tuval.devoluciones.Imp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tuval.api.web.models.dao.tuval.devoluciones.InvoiceReturnDao;
import tuval.api.web.models.dto.devolucion.ItemBinResponseDto;
import tuval.api.web.models.dto.devolucion.notaCredito.DocumentNcDto;
import tuval.api.web.models.entity.tuval.devoluciones.DevolutionResponse;
import tuval.api.web.models.request.devoluciones.DetailDevolutionRequest;
import tuval.api.web.models.request.devoluciones.DetailNoteCreditRequest;
import tuval.api.web.models.request.devoluciones.DevolutionRequest;
import tuval.api.web.models.request.devoluciones.HeaderNoteCreditRequest;
import tuval.api.web.models.service.tuval.devoluciones.InvoiceReturnService;
import tuval.api.web.models.service.tuval.devoluciones.Utils.InvoiceReturnUtils;

@Service
public class InvoiceReturnServiceImp implements InvoiceReturnService {

  @Autowired private InvoiceReturnDao invoiceReturnDao;

  public ResponseEntity<Map<String, Object>> generacionNotaCreditoVTA(
      Map<String, Object> cabecera, List<Map<String, Object>> detalle) {
    Map<String, Object> response = new HashMap<>();

    try {

      if (cabecera.isEmpty() || cabecera == null) {
        return InvoiceReturnUtils.buildError(
            "No se puede procesar un documento que no tiene cabecera.");
      }
      if (detalle.isEmpty() || detalle == null) {
        return InvoiceReturnUtils.buildError("Detalle vacío o no proporcionado.");
      }

      String document =
          InvoiceReturnUtils.getRequiredField(
              cabecera, "NC_VTA_DOCU_ORIGEN", "001", "Documento de origen no proporcionado.");
      String tipo =
          InvoiceReturnUtils.getRequiredField(
              cabecera, "NC_VTA_ID_TIPO", "002", "Tipo de documento no proporcionado.");
      String fechaOriginal =
          InvoiceReturnUtils.getRequiredField(
              cabecera, "NC_VTA_FECHA", "004", "Fecha no proporcionada.");
      String fechaFormateada = fechaOriginal.replace("-", "/");

      ResponseEntity<Map<String, Object>> validateResponse =
          invoiceReturnDao.ValidateGenerationDocumentVTANC(document, tipo);
      Map<String, Object> validateBody = validateResponse.getBody();
      if (validateBody != null && validateBody.get("data") != null) {
        return InvoiceReturnUtils.buildError("El documento ya tiene una nota de crédito generada.");
      }

      invoiceReturnDao.eliminacionNcCabDet(document, tipo);
      String puntoVenta =
          InvoiceReturnUtils.getRequiredField(
              cabecera, "NC_VTA_PUNTO_VENTA", "002.3", "Punto de venta no proporcionado.");
      String docId =
          InvoiceReturnUtils.getRequiredField(
              cabecera, "NC_VTA_DOC_ID", "002.4", "ID de documento no proporcionado.");
      String cliente =
          InvoiceReturnUtils.getRequiredField(
              cabecera, "NC_VTA_ID_CLIENTE", "003", "Campo NC_VTA_ID_CLIENTE vacío");
      String vendedor =
          InvoiceReturnUtils.getRequiredField(
              cabecera, "NC_VTA_ID_VENDEDOR", "003.1", "Campo NC_VTA_ID_VENDEDOR vacío");
      String direccion =
          InvoiceReturnUtils.getRequiredField(
              cabecera, "NC_VTA_ID_DIRECCION", "003.2", "Campo NC_VTA_ID_DIRECCION vacío");
      String condPago =
          InvoiceReturnUtils.getRequiredField(
              cabecera, "NC_VTA_COND_PAGO", "003.3", "Campo NC_VTA_COND_PAGO vacío");
      String notaDocu = InvoiceReturnUtils.getStringFromMap(cabecera, "NC_VTA_NOTA_DOCU");
      String bodega =
          InvoiceReturnUtils.getRequiredField(
              cabecera, "NC_VTA_BODEGA", "005", "Campo NC_VTA_BODEGA vacío");

      double subtotal =
          parseDoubleOrFail(
              cabecera, "NC_VTA_SUBTOTAL", document, tipo, "006", "Subtotal inválido");
      double impuesto =
          parseDoubleOrFail(
              cabecera, "NC_VTA_IMPUESTO", document, tipo, "014", "Impuesto inválido");
      double descuentoComercial =
          parseDoubleOrFail(
              cabecera,
              "NC_VTA_DESCUENTO_COMERCIAL",
              document,
              tipo,
              "014.1",
              "Descuento comercial inválido");
      double totalCab =
          parseDoubleOrFail(cabecera, "NC_VTA_TOTAL", document, tipo, "013", "Total inválido");

      ResponseEntity<Map<String, Object>> responseCabeceraNotaCredito =
          invoiceReturnDao.procesarCabeceraNotaCredito(
              document,
              tipo,
              puntoVenta,
              docId,
              cliente,
              vendedor,
              direccion,
              condPago,
              notaDocu,
              fechaFormateada,
              bodega,
              subtotal,
              descuentoComercial,
              impuesto,
              totalCab);

      for (Map<String, Object> det : detalle) {
        String docuDet =
            InvoiceReturnUtils.getRequiredField(
                det, "NC_VTA_DET_DOCU_ORIGEN", "002.1.2", "Campo NC_VTA_DET_DOCU_ORIGEN vacío");
        String tipoDet =
            InvoiceReturnUtils.getRequiredField(
                det, "NC_VTA_DET_ID_TIPO", "002.1.3", "Campo NC_VTA_DET_ID_TIPO vacío");
        double linea =
            parseDoubleFacturaOrFail(
                det,
                "NC_VTA_DET_LINEA",
                document,
                tipo,
                "077",
                "La línea no puede ser 0 o menor a 0");
        String articulo =
            InvoiceReturnUtils.getRequiredField(
                det, "NC_VTA_DET_ARTICULO", "007", "Campo artículo vacío");
        String descripcion =
            InvoiceReturnUtils.getRequiredField(
                det, "NC_VTA_DET_DESC_ARTICULO", "007.01", "Campo descripción vacío");
        String unidad =
            InvoiceReturnUtils.getRequiredField(
                det, "NC_VTA_DET_UNIDAD_MEDIDA", "008", "Campo unidad vacío");
        String bodegaDet =
            InvoiceReturnUtils.getRequiredField(
                det, "NC_VTA_DET_BODEGA", "009", "Campo bodega vacío");
        double cantidad =
            parseDoubleOrFail(
                det, "NC_VTA_DET_CANTIDAD", document, tipo, "010", "Cantidad inválida");

        ResponseEntity<Map<String, Object>> tipoProductoResponse =
            invoiceReturnDao.obtenerTipoProducto(articulo);
        Map<String, Object> responseProductType = tipoProductoResponse.getBody();

        int tipoProducto = 0;
        double costo = 0.0;
        if (responseProductType != null && responseProductType != null) {
          Object tipoProductoObj = responseProductType.get("tipo_producto");

          if (tipoProductoObj instanceof Integer) {
            tipoProducto = (Integer) tipoProductoObj;
          } else {
            throw new Exception("Tipo de producto inválido o no encontrado.");
          }
          if (tipoProducto == 1) {
            costo =
                parseDoubleOrFail(
                    det,
                    "NC_VTA_DET_COSTO_UNITARIO",
                    document,
                    tipo,
                    "011",
                    "El valor del costo unitario tiene que ser mayor a 0");
            if (costo <= 0.0) {
              invoiceReturnDao.IncidenciaNcVta(
                  document, tipo, "011", "El valor del costo unitario tiene que ser mayor a 0");
              return InvoiceReturnUtils.buildError(
                  "El valor del costo unitario tiene que ser mayor a 0");
            }
          }
        } else {
          throw new Exception("No se pudo obtener el tipo de producto.");
        }

        double precio =
            parseDoubleOrFail(
                det, "NC_VTA_DET_PRECIO_UNITARIO", document, tipo, "012", "Precio inválido");
        double descuento =
            parseDoubleOrFail(
                det, "NC_VTA_DET_DESCUENTO", document, tipo, "013", "Descuento inválido");
        double iva =
            parseDoubleOrFail(
                det, "NC_VTA_DET_IMPUESTO_IVA", document, tipo, "014", "IVA inválido");
        double totalDet =
            parseDoubleOrFail(
                det, "NC_VTA_DET_TOTAL", document, tipo, "015", "Total detalle inválido");

        ResponseEntity<Map<String, Object>> responseDetalleNotaCredito =
            invoiceReturnDao.procesarDetalleNotaCredito(
                docuDet,
                tipoDet,
                linea,
                articulo,
                descripcion,
                unidad,
                bodegaDet,
                cantidad,
                costo,
                precio,
                descuento,
                iva,
                totalDet);
        if (!InvoiceReturnUtils.isSuccess(responseDetalleNotaCredito)) {
          return InvoiceReturnUtils.buildError(
              "Error procesando detalle: "
                  + (responseDetalleNotaCredito.getBody() != null
                      ? responseDetalleNotaCredito.getBody().get("message")
                      : "Respuesta nula del detalle"));
        }
      }
      var finalResp = invoiceReturnDao.generarNotaCredito(document, tipo);
      if (!InvoiceReturnUtils.isSuccess(finalResp)) {
        return InvoiceReturnUtils.buildError("Error al generar nota de crédito.");
      }

      response.put("success", true);
      response.put("message", "Nota de crédito generada correctamente");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return InvoiceReturnUtils.buildException(e);
    }
  }

  public ResponseEntity<Map<String, Object>> getResponseCreditNote(String document, String tipo) {
    // Map<String, Object> response = new HashMap<>();
    try {
      ResponseEntity<Map<String, Object>> respuesta =
          invoiceReturnDao.respuestaNotaCredito(document, tipo);
      if (respuesta.getBody() != null && respuesta.getBody().get("success") != null) {
        return respuesta;
      } else {
        return InvoiceReturnUtils.buildError(
            "Error al obtener la respuesta de la nota de crédito.");
      }
    } catch (Exception e) {
      return InvoiceReturnUtils.buildException(e);
    }
  }

  private double parseDoubleOrFail(
      Map<String, Object> map, String key, String doc, String tipo, String code, String msg)
      throws Exception {
    Object value = map.get(key);
    if (value == null) {
      invoiceReturnDao.IncidenciaNcVta(doc, tipo, code, msg);
      throw new Exception(msg);
    }
    try {
      return Double.parseDouble(value.toString());
    } catch (NumberFormatException e) {
      invoiceReturnDao.IncidenciaNcVta(doc, tipo, code, "Formato numérico inválido");
      throw new Exception("Formato numérico inválido");
    }
  }

  private double parseDoubleFacturaOrFail(
      Map<String, Object> map, String key, String doc, String tipo, String code, String msg)
      throws Exception {
    Object value = map.get(key);
    if (value == null) {
      invoiceReturnDao.guardarIncidenciaFactura(doc, tipo, code, msg);
      throw new Exception(msg);
    }
    try {
      return Double.parseDouble(value.toString());
    } catch (NumberFormatException e) {
      invoiceReturnDao.guardarIncidenciaFactura(doc, tipo, code, "Formato numérico inválido");
      throw new Exception("Formato numérico inválido");
    }
  }

  @Override
  public DevolutionResponse generateAjustmentDevolutionService(DevolutionRequest request) {
    DevolutionResponse responseHeader =
        invoiceReturnDao.executeSpStoreHeaderDevolution(request.getCabecera(), 17);
    //    return invoiceReturnDao.executeSpStoreHeaderDevolution(request.getCabecera(), 17);
    if (request.getDetalle() != null) {
      for (DetailDevolutionRequest det : request.getDetalle()) {
        invoiceReturnDao.executeSpStoreDetaillDevolution(
            det, request.getCabecera(), responseHeader.getIdMovimiento(), 1, 18);
      }
    }
    return invoiceReturnDao.executeSpGenerateMovimentDevolution(
        request.getCabecera(), responseHeader.getIdMovimiento(), 19);
  }

  @Override
  public ItemBinResponseDto getResponseItemBin(String item, String bodega) {
    return invoiceReturnDao.consultItemBin(item, bodega, 16);
  }

  @Override
  public ResponseEntity<Map<String, Object>> getClientGroupEconomicService(String ruc) {
    return invoiceReturnDao.getGroupEconomicCLiente(ruc);
  }


  @Override
  public DocumentNcDto generateNoteCreditService(HeaderNoteCreditRequest cab, List<DetailNoteCreditRequest> det) {
    DocumentNcDto hasDoc =  invoiceReturnDao.executeSpValidateExistsNc(cab);
    if (!hasDoc.getDocGenerado().isEmpty() || !hasDoc.getDocGenerado().isBlank()) {
       throw new RuntimeException("Ya existe un numero de Documento Generado para la devolución : " + cab.getDocOrigen() + " y numero de documento : " + hasDoc.getDocGenerado());
    }
     invoiceReturnDao.executeSpDeleteExistenceNc(cab);
    return hasDoc;
  }
}
