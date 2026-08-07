package tuval.api.web.models.service.tuval.cobros;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tuval.api.web.models.dao.tuval.cobros.CollectionRegisterDao;
import tuval.api.web.models.request.cobros.cobrosFacturas.CollectionDefaultInvoiceRequest;
import tuval.api.web.models.request.cobros.cobrosFacturas.CollectionDetailInvoicesRequest;
import tuval.api.web.models.request.cobros.cobrosFacturas.CollectionRetentionInvoiceRequest;

@Service
@RequiredArgsConstructor
public class CollectionRegisterService {

  private final CollectionRegisterDao collectionRegisterDao;

  // aqui registra efectivo , tarjeta, deposito
  public String registerCashCollection(CollectionDefaultInvoiceRequest request) {
    return collectionRegisterDao.registerCashCollection(request, 2);
  }

  // aqui registra aplicaciones
  public String registerApplicationCollection(CollectionDetailInvoicesRequest request) {
    return collectionRegisterDao.registerApplicationCollection(request, 6);
  }

  // aqui registra retenciones
  @Transactional(transactionManager = "tuvalTransactionManager", rollbackFor = Exception.class)
  public String registerRetentionCollection(CollectionRetentionInvoiceRequest request) {
    validateRetentionRequest(request);

    String headerMessage =
        collectionRegisterDao.registerRetentionHeaderCollection(request.getCabecera(), 1);
    if (headerMessage == null || headerMessage.isBlank()) {
      throw new IllegalStateException(
          "No se pudo confirmar el registro de la cabecera de la retencion.");
    }

    collectionRegisterDao.registerApplicationCollection(request.getDetalle(), 2);
    return "Registro de retencion guardado correctamente.";
  }

  private void validateRetentionRequest(CollectionRetentionInvoiceRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("El payload de retencion es obligatorio.");
    }
    if (request.getCabecera() == null) {
      throw new IllegalArgumentException("La cabecera de la retencion es obligatoria.");
    }
    if (request.getDetalle() == null) {
      throw new IllegalArgumentException("El detalle de la retencion es obligatorio.");
    }
  }
}
