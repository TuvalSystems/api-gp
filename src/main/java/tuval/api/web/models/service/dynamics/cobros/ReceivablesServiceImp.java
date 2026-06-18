package tuval.api.web.models.service.dynamics.cobros;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import tuval.api.web.models.dao.dynamics.cobros.CollectionDao;
import tuval.api.web.models.dto.cobros.CustomerCollectionDto;
import tuval.api.web.models.dto.cobros.CustomerInvoiceCheckReceiptDto;
import tuval.api.web.models.dto.cobros.CustomerPendingApplicationDocumentDto;
import tuval.api.web.models.request.cobros.CustomerCollectionRequest;

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
  public CustomerPendingApplicationDocumentDto getPendingApplicationDocuments(String documento, String ruc){
    return collectionDao.executeSpCxC34(documento, ruc);
  }
}
