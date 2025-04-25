package tuval.api.web.models.service.tuval.devoluciones.Imp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import tuval.api.web.models.dao.tuval.devoluciones.InvoiceReturnDao;
import tuval.api.web.models.service.tuval.devoluciones.InvoiceReturnService;
import tuval.api.web.models.service.tuval.devoluciones.Utils.InvoiceUtils;

@Service
public class InvoiceReturnServiceImp implements InvoiceReturnService {

    @Autowired
    private InvoiceReturnDao invoiceReturnDao;

    public ResponseEntity<Map<String, Object>> generacionNotaCreditoVTA(Map<String, Object> cabecera,
            List<Map<String, Object>> detalle) {
        Map<String, Object> response = new HashMap<>();

        try {

            if (cabecera.isEmpty() || cabecera == null) {
                return InvoiceUtils.buildError("No se puede procesar un documento que no tiene cabecera.");
            }
            if (detalle.isEmpty() || detalle == null) {
                return InvoiceUtils.buildError("Detalle vacío o no proporcionado.");
            }

            String document = InvoiceUtils.getRequiredField(cabecera, "NC_VTA_DOCU_ORIGEN", "001",
                    "Documento de origen no proporcionado.");
            String tipo = InvoiceUtils.getRequiredField(cabecera, "NC_VTA_ID_TIPO", "002", "Tipo de documento no proporcionado.");
            String fechaOriginal = InvoiceUtils.getRequiredField(cabecera, "NC_VTA_FECHA", "004", "Fecha no proporcionada.");
            String fechaFormateada = fechaOriginal.replace("-", "/");

            if (invoiceReturnDao.ValidateGenerationDocumentVTANC(document, tipo).getBody().get("data") != null) {
                invoiceReturnDao.eliminacionNcCabDet(document, tipo);
                return InvoiceUtils.buildError("El documento ya tiene una nota de crédito generada.");
            }

            String puntoVenta = InvoiceUtils.getRequiredField(cabecera, "NC_VTA_PUNTO_VENTA", "002.3",
                    "Punto de venta no proporcionado.");
            String docId = InvoiceUtils.getRequiredField(cabecera, "NC_VTA_DOC_ID", "002.4", "ID de documento no proporcionado.");
            String cliente = InvoiceUtils.getRequiredField(cabecera, "NC_VTA_ID_CLIENTE", "003", "Campo NC_VTA_ID_CLIENTE vacío");
            String vendedor = InvoiceUtils.getRequiredField(cabecera, "NC_VTA_ID_VENDEDOR", "003.1",
                    "Campo NC_VTA_ID_VENDEDOR vacío");
            String direccion = InvoiceUtils.getRequiredField(cabecera, "NC_VTA_ID_DIRECCION", "003.2",
                    "Campo NC_VTA_ID_DIRECCION vacío");
            String condPago = InvoiceUtils.getRequiredField(cabecera, "NC_VTA_COND_PAGO", "003.3", "Campo NC_VTA_COND_PAGO vacío");
            String notaDocu = InvoiceUtils.getStringFromMap(cabecera, "NC_VTA_NOTA_DOCU");
            String bodega = InvoiceUtils.getRequiredField(cabecera, "NC_VTA_BODEGA", "005", "Campo NC_VTA_BODEGA vacío");

            double subtotal = parseDoubleOrFail(cabecera, "NC_VTA_SUBTOTAL", document, tipo, "006",
                    "Subtotal inválido");
            double impuesto = parseDoubleOrFail(cabecera, "NC_VTA_IMPUESTO", document, tipo, "014",
                    "Impuesto inválido");
            double descuentoComercial = parseDoubleOrFail(cabecera, "NC_VTA_DESCUENTO_COMERCIAL", document, tipo,
                    "014.1", "Descuento comercial inválido");
            double totalCab = parseDoubleOrFail(cabecera, "NC_VTA_TOTAL", document, tipo, "013", "Total inválido");

            ResponseEntity<Map<String, Object>> responseCabeceraNotaCredito = invoiceReturnDao
                    .procesarCabeceraNotaCredito(document,
                            tipo, puntoVenta, docId, cliente, vendedor, direccion, condPago, notaDocu, fechaFormateada,
                            bodega, subtotal, descuentoComercial, impuesto, totalCab);

            for (Map<String, Object> det : detalle) {
                String docuDet = InvoiceUtils.getRequiredField(det, "NC_VTA_DET_DOCU_ORIGEN", "002.1.2",
                        "Campo NC_VTA_DET_DOCU_ORIGEN vacío");
                String tipoDet = InvoiceUtils.getRequiredField(det, "NC_VTA_DET_ID_TIPO", "002.1.3",
                        "Campo NC_VTA_DET_ID_TIPO vacío");
                double linea = parseDoubleFacturaOrFail(det, "NC_VTA_DET_LINEA", document, tipo, "077",
                        "La línea no puede ser 0 o menor a 0");
                String articulo = InvoiceUtils.getRequiredField(det, "NC_VTA_DET_ARTICULO", "007", "Campo artículo vacío");
                String descripcion = InvoiceUtils.getRequiredField(det, "NC_VTA_DET_DESC_ARTICULO", "007.01",
                        "Campo descripción vacío");
                String unidad = InvoiceUtils.getRequiredField(det, "NC_VTA_DET_UNIDAD_MEDIDA", "008", "Campo unidad vacío");
                String bodegaDet = InvoiceUtils.getRequiredField(det, "NC_VTA_DET_BODEGA", "009", "Campo bodega vacío");
                double cantidad = parseDoubleOrFail(det, "NC_VTA_DET_CANTIDAD", document, tipo, "010",
                        "Cantidad inválida");

                ResponseEntity<Map<String, Object>> tipoProductoResponse = invoiceReturnDao
                        .obtenerTipoProducto(articulo);
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
                        costo = parseDoubleOrFail(det, "NC_VTA_DET_COSTO_UNITARIO", document, tipo, "011",
                                "El valor del costo unitario tiene que ser mayor a 0");
                        if (costo <= 0.0) {
                            invoiceReturnDao.IncidenciaNcVta(document, tipo, "011",
                                    "El valor del costo unitario tiene que ser mayor a 0");
                            return InvoiceUtils.buildError("El valor del costo unitario tiene que ser mayor a 0");
                        }
                    }
                } else {
                    throw new Exception("No se pudo obtener el tipo de producto.");
                }

                double precio = parseDoubleOrFail(det, "NC_VTA_DET_PRECIO_UNITARIO", document, tipo, "012",
                        "Precio inválido");
                double descuento = parseDoubleOrFail(det, "NC_VTA_DET_DESCUENTO", document, tipo, "013",
                        "Descuento inválido");
                double iva = parseDoubleOrFail(det, "NC_VTA_DET_IMPUESTO_IVA", document, tipo, "014", "IVA inválido");
                double totalDet = parseDoubleOrFail(det, "NC_VTA_DET_TOTAL", document, tipo, "015",
                        "Total detalle inválido");

                ResponseEntity<Map<String, Object>> responseDetalleNotaCredito = invoiceReturnDao
                        .procesarDetalleNotaCredito(docuDet, tipoDet, linea,
                                articulo, descripcion, unidad, bodegaDet, cantidad,
                                costo, precio,
                                descuento,
                                iva, totalDet);
                if (!InvoiceUtils.isSuccess(responseDetalleNotaCredito)) {
                    return InvoiceUtils.buildError(
                            "Error procesando detalle: " + (responseDetalleNotaCredito.getBody() != null
                                    ? responseDetalleNotaCredito.getBody().get("message")
                                    : "Respuesta nula del detalle"));
                }

            }
            var finalResp = invoiceReturnDao.generarNotaCredito(document, tipo);
            if (!InvoiceUtils.isSuccess(finalResp)) {
                return InvoiceUtils.buildError("Error al generar nota de crédito.");
            }

            response.put("success", true);
            response.put("message", "Nota de crédito generada correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return InvoiceUtils.buildException(e);
        }
    }


    public ResponseEntity<Map<String, Object>> getResponseCreditNote(String document, String tipo) {
        // Map<String, Object> response = new HashMap<>();
        try {
            ResponseEntity<Map<String, Object>> respuesta = invoiceReturnDao.respuestaNotaCredito(document, tipo);
            if (respuesta.getBody() != null && respuesta.getBody().get("success") != null) {
                return respuesta;
            } else {
                return InvoiceUtils.buildError("Error al obtener la respuesta de la nota de crédito.");
            }
        } catch (Exception e) {
            return InvoiceUtils.buildException(e);
        }
        
    }


    private double parseDoubleOrFail(Map<String, Object> map, String key, String doc, String tipo, String code,
            String msg) throws Exception {
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

    private double parseDoubleFacturaOrFail(Map<String, Object> map, String key, String doc, String tipo, String code,
            String msg) throws Exception {
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

}
