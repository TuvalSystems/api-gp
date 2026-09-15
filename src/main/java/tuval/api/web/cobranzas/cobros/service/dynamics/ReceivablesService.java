package tuval.api.web.cobranzas.cobros.service.dynamics;

import java.util.List;

import tuval.api.web.cobranzas.cobros.dto.CustomerCollectionDto;
import tuval.api.web.cobranzas.cobros.dto.CustomerInvoiceCheckReceiptDto;
import tuval.api.web.cobranzas.cobros.dto.CustomerPendingApplicationDocumentDto;
import tuval.api.web.cobranzas.cobros.request.CustomerCollectionRequest;

public interface ReceivablesService {

  List<CustomerCollectionDto> getInvoicePaymentApplications(CustomerCollectionRequest payload);

  List<CustomerInvoiceCheckReceiptDto> getCheckInvoicePayments(CustomerCollectionRequest payload);

  CustomerPendingApplicationDocumentDto getPendingApplicationDocuments(
      String documento, String ruc);

  public List<CustomerInvoiceCheckReceiptDto> getCheckInvoicePendingPayments(
      CustomerCollectionRequest payload);
}
