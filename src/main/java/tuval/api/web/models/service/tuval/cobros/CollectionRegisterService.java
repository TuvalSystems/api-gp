package tuval.api.web.models.service.tuval.cobros;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tuval.api.web.models.dao.tuval.cobros.CollectionRegisterDao;
import tuval.api.web.models.request.cobros.cobrosFacturas.CollectionCashInvoiceRequest;

@Service
@RequiredArgsConstructor
public class CollectionRegisterService {

  private final CollectionRegisterDao collectionRegisterDao;

  public String registerCashCollection(CollectionCashInvoiceRequest request) {
    return collectionRegisterDao.registerCashCollection(request, 2);
  }
}
