package tuval.api.web.models.service.tuval.transacciones;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.ws.rs.core.Response;
import tuval.api.web.models.dao.tuval.transacciones.transactionDao;
import tuval.api.web.models.dto.transacciones.facturacionVenta.ReceivedInvoiceRequest;
import tuval.api.web.models.dto.transacciones.facturacionVenta.ResponseConsultInvoiceRequest;
import tuval.api.web.models.dto.transacciones.reciboCompraL.DetailReceivedLRequest;
import tuval.api.web.models.dto.transacciones.reciboCompraL.ReceivedSalesLRequest;
import tuval.api.web.models.dto.transacciones.reciboCompraL.validateReceidLRequestEntity;
import tuval.api.web.models.entity.tuval.transacciones.facturacionVenta.responseInvoiceVtaEntity;
import tuval.api.web.models.entity.tuval.transacciones.facturacionVenta.validateGenerateInvoiceResponseEntity;
import tuval.api.web.models.entity.tuval.transacciones.reciboCompraL.validateReceivedLResponseEntity;
import tuval.api.web.models.entity.tuval.transacciones.reciboCompraL.validateResponseEntity;

@Service
public class transactionService {
    @Autowired

    private transactionDao transactionDao;

    public validateResponseEntity validateReceivedL(validateReceidLRequestEntity request) {
        return transactionDao.executeSpValidateGenerateReceivedItemL(request);
    }

    public validateResponseEntity deleteReceivedL(validateReceidLRequestEntity request) {
        return transactionDao.executeSpDeleteReceivedHeaderAndDetail(request);
    }

    @Transactional(rollbackFor = Exception.class)
    public validateReceivedLResponseEntity storeReceivedL(ReceivedSalesLRequest request) {

        transactionDao.executeSpStoreHeaderReceivedL(request.getCabecera());
        if (request.getDetalle() != null) {
            for (DetailReceivedLRequest det : request.getDetalle()) {
                transactionDao.executeSpStoreDetailReceivedL(det);

            }
        }
        var resp = new validateReceivedLResponseEntity();
        resp.setCodigo("0");
        resp.setMensaje("Recibo guardado correctamente");
        return resp;
    }

    public validateReceivedLResponseEntity processReceivedL(validateReceidLRequestEntity request) {
        transactionDao.executeSpGenerateProcessReceivedL(request);
        var resp = new validateReceivedLResponseEntity();
        resp.setCodigo("0");
        resp.setMensaje("Recibo procesado correctamente");
        return resp;
    }

    public validateResponseEntity responseReceivedL(validateReceidLRequestEntity request) {
        return transactionDao.executeSpResponseReceivedL(request);

    }

    /***
     * 
     * 
     * GENERACION DE FACTURA DE VENTA
     */

    @Transactional(rollbackFor = Exception.class)
    public validateGenerateInvoiceResponseEntity storeInvoice(ReceivedInvoiceRequest request) {
        transactionDao.executeSpStoreHeaderInvoice(request.getCabecera());
        if (request.getDetalle() != null) {
            for (var det : request.getDetalle()) {
                transactionDao.executeSpStoreDetailInvoice(det);
            }
        }
        transactionDao.executSpGenerateInvoiceVta(request.getCabecera());
        var resp = new validateGenerateInvoiceResponseEntity();
        resp.setMensaje("Factura guardada correctamente");
        return resp;
    }

    @Transactional(rollbackFor = Exception.class)
    public responseInvoiceVtaEntity consultInvoiceVta(ResponseConsultInvoiceRequest request) {
        return transactionDao.executeSpResponseInvoiceVta(request);
    }

    @Transactional(rollbackFor = Exception.class)
    public validateGenerateInvoiceResponseEntity deleteInvoiceVta(ResponseConsultInvoiceRequest request) {
        transactionDao.executeSpDeleteInvoiceVta(request);
        var resp = new validateGenerateInvoiceResponseEntity();
        resp.setMensaje("Factura eliminada correctamente");
        return resp;
    }

}
