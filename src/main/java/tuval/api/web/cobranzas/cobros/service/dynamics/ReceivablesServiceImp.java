package tuval.api.web.cobranzas.cobros.service.dynamics;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tuval.api.web.cobranzas.cobros.dao.dynamics.CollectionDao;
import tuval.api.web.cobranzas.cobros.dto.CustomerCollectionDto;
import tuval.api.web.cobranzas.cobros.dto.CustomerInvoiceCheckReceiptDto;
import tuval.api.web.cobranzas.cobros.dto.CustomerInvoicePendingToPostDto;
import tuval.api.web.cobranzas.cobros.dto.CustomerPendingApplicationDocumentDto;
import tuval.api.web.cobranzas.cobros.request.CustomerCollectionRequest;

@Service
@RequiredArgsConstructor
public class ReceivablesServiceImp implements ReceivablesService {
  private final CollectionDao collectionDao;

  @Override
  public List<CustomerCollectionDto> getInvoicePaymentApplications(
      CustomerCollectionRequest payload) {

    return collectionDao.executeSpCxC32(payload.getDocumento(), payload.getRuc());
  }

  @Override
  public List<CustomerInvoiceCheckReceiptDto> getCheckInvoicePayments(
      CustomerCollectionRequest payload) {

    return collectionDao.executeSpCxC33(payload.getDocumento(), payload.getRuc());
  }

  @Override
  public CustomerPendingApplicationDocumentDto getPendingApplicationDocuments(
      String documento, String ruc) {
    return collectionDao.executeSpCxC34(documento, ruc);
  }

  @Override
  public List<CustomerInvoiceCheckReceiptDto> getCheckInvoicePendingPayments(
      CustomerCollectionRequest payload) {

    return collectionDao.executeSpCxC35(payload.getDocumento(), payload.getRuc());
  }

   @Override
  public List<CustomerInvoicePendingToPostDto> getPendingPostingDocuments(
      CustomerCollectionRequest payload) {

    return collectionDao.executeSpCxC36(payload.getDocumento(), payload.getRuc());
  }

  
}
