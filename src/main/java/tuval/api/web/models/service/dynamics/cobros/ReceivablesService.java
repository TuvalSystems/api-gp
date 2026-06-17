package tuval.api.web.models.service.dynamics.cobros;

import java.util.List;
import tuval.api.web.models.dto.cobros.CustomerCollectionDto;
import tuval.api.web.models.dto.cobros.CustomerInvoiceCheckReceiptDto;
import tuval.api.web.models.dto.cobros.CustomerPendingApplicationDocumentDto;
import tuval.api.web.models.request.cobros.CustomerCollectionRequest;

public interface ReceivablesService {

  List<CustomerCollectionDto> getInvoicePaymentApplications(CustomerCollectionRequest payload);

  List<CustomerInvoiceCheckReceiptDto> getCheckInvoicePayments(CustomerCollectionRequest payload);

  CustomerPendingApplicationDocumentDto getPendingApplicationDocuments(
      String documento, String ruc);
}
