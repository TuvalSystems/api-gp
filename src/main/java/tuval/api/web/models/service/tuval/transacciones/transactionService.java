package tuval.api.web.models.service.tuval.transacciones;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tuval.api.web.models.dao.tuval.transacciones.transactionDao;
import tuval.api.web.models.dto.transacciones.facturacionVenta.responseInvoiceVtaDto;
import tuval.api.web.models.dto.transacciones.facturacionVenta.validateGenerateInvoiceDto;
import tuval.api.web.models.dto.transacciones.reciboCompraL.validateReceivedLDto;
import tuval.api.web.models.dto.transacciones.reciboCompraL.validateDto;
import tuval.api.web.models.request.transacciones.facturacionVenta.ReceivedInvoiceRequest;
import tuval.api.web.models.request.transacciones.facturacionVenta.ResponseConsultInvoiceRequest;
import tuval.api.web.models.request.transacciones.reciboCompraL.DetailReceivedLRequest;
import tuval.api.web.models.request.transacciones.reciboCompraL.ReceivedSalesLRequest;
import tuval.api.web.models.request.transacciones.reciboCompraL.validateReceidLRequest;

@Service
public class transactionService {
    @Autowired

    private transactionDao transactionDao;

    public validateDto validateReceivedL(validateReceidLRequest request) {
        return transactionDao.executeSpValidateGenerateReceivedItemL(request);
    }

    public validateDto deleteReceivedL(validateReceidLRequest request) {
        return transactionDao.executeSpDeleteReceivedHeaderAndDetail(request);
    }

    @Transactional(rollbackFor = Exception.class)
    public validateReceivedLDto storeReceivedL(ReceivedSalesLRequest request) {

        transactionDao.executeSpStoreHeaderReceivedL(request.getCabecera());
        if (request.getDetalle() != null) {
            for (DetailReceivedLRequest det : request.getDetalle()) {
                transactionDao.executeSpStoreDetailReceivedL(det);

            }
        }
        var resp = new validateReceivedLDto();
        resp.setCodigo("0");
        resp.setMensaje("Recibo guardado correctamente");
        return resp;
    }

    public validateReceivedLDto processReceivedL(validateReceidLRequest request) {
        transactionDao.executeSpGenerateProcessReceivedL(request);
        var resp = new validateReceivedLDto();
        resp.setCodigo("0");
        resp.setMensaje("Recibo procesado correctamente");
        return resp;
    }

    public validateDto responseReceivedL(validateReceidLRequest request) {
        return transactionDao.executeSpResponseReceivedL(request);

    }

    /***
     * 
     * 
     * GENERACION DE FACTURA DE VENTA
     */

    @Transactional(rollbackFor = Exception.class)
    public validateGenerateInvoiceDto storeInvoice(ReceivedInvoiceRequest request) {
        transactionDao.executeSpStoreHeaderInvoice(request.getCabecera());
        if (request.getDetalle() != null) {
            for (var det : request.getDetalle()) {
                transactionDao.executeSpStoreDetailInvoice(det);
            }
        }
        transactionDao.executSpGenerateInvoiceVta(request.getCabecera());
        var resp = new validateGenerateInvoiceDto();
        resp.setMensaje("Factura guardada correctamente");
        return resp;
    }

    @Transactional(rollbackFor = Exception.class)
    public responseInvoiceVtaDto consultInvoiceVta(ResponseConsultInvoiceRequest request) {
        return transactionDao.executeSpResponseInvoiceVta(request);
    }

    @Transactional(rollbackFor = Exception.class)
    public validateGenerateInvoiceDto deleteInvoiceVta(ResponseConsultInvoiceRequest request) {
        transactionDao.executeSpDeleteInvoiceVta(request);
        var resp = new validateGenerateInvoiceDto();
        resp.setMensaje("Factura eliminada correctamente");
        return resp;
    }

}
