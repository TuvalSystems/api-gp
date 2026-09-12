package tuval.api.web.transacciones.service.tuval;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import tuval.api.web.bodega.recepciones.dto.StatusReceptionOrderItemLDto;
import tuval.api.web.bodega.recepciones.dto.validateDto;
import tuval.api.web.bodega.recepciones.dto.validateReceivedLDto;
import tuval.api.web.bodega.recepciones.request.DetailReceivedLRequest;
import tuval.api.web.bodega.recepciones.request.ReceivedSalesLRequest;
import tuval.api.web.bodega.recepciones.request.validateReceidLRequest;
import tuval.api.web.transacciones.dao.tuval.transactionDao;
import tuval.api.web.ventas.facturacion.dto.responseInvoiceVtaDto;
import tuval.api.web.ventas.facturacion.dto.validateGenerateInvoiceDto;
import tuval.api.web.ventas.facturacion.request.ReceivedInvoiceRequest;
import tuval.api.web.ventas.facturacion.request.ResponseConsultInvoiceRequest;

@Service
@RequiredArgsConstructor
public class transactionService {

    private final transactionDao transactionDao;

    public validateDto validateReceivedL(validateReceidLRequest request) {
        return transactionDao.executeSpValidateGenerateReceivedItemL(request);
    }

    public validateDto deleteReceivedL(validateReceidLRequest request) {
        return transactionDao.executeSpDeleteReceivedHeaderAndDetail(request);
    }

    @Transactional(transactionManager = "tuvalTransactionManager", rollbackFor = Exception.class)
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


    public StatusReceptionOrderItemLDto stateReceivedLService(validateReceidLRequest request) {
        return transactionDao.executeSpRepsonseStatusReceptionOrderItemLDao(request);
    }

    /***
     * 
     * 
     * GENERACION DE FACTURA DE VENTA
     */

    @Transactional(transactionManager = "tuvalTransactionManager", rollbackFor = Exception.class)
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

    @Transactional(transactionManager = "tuvalTransactionManager", rollbackFor = Exception.class)
    public responseInvoiceVtaDto consultInvoiceVta(ResponseConsultInvoiceRequest request) {
        return transactionDao.executeSpResponseInvoiceVta(request);
    }

    @Transactional(transactionManager = "tuvalTransactionManager", rollbackFor = Exception.class)
    public validateGenerateInvoiceDto deleteInvoiceVta(ResponseConsultInvoiceRequest request) {
        transactionDao.executeSpDeleteInvoiceVta(request);
        var resp = new validateGenerateInvoiceDto();
        resp.setMensaje("Factura eliminada correctamente");
        return resp;
    }

}
