package tuval.api.web.models.service.dynamics.cobros;

import java.util.List;


import tuval.api.web.models.dto.cobros.CustomerCollectionDto;
import tuval.api.web.models.request.cobros.CustomerCollectionRequest;

public interface  ReceivablesService {

    List<CustomerCollectionDto> getInvoicePaymentApplications(CustomerCollectionRequest payload);
    
}
