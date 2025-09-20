package tuval.api.web.models.dao.tuval.cobros;
import tuval.api.web.models.entity.tuval.cobros.CheckValidatorEntity;
import tuval.api.web.models.entity.tuval.cobros.ClientOverdueDocumentEntity;

public interface CreditCheckDao {
    public CheckValidatorEntity getConsultCreditValidation(String ruc, String idCompania, int modo);


    public ClientOverdueDocumentEntity executeSpConsultExpiredClientDocument(String ruc, String idCompania, int modo);
}
