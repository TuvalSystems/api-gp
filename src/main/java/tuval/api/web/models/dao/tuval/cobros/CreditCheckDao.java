package tuval.api.web.models.dao.tuval.cobros;
import tuval.api.web.models.entity.tuval.cobros.CheckValidatorEntity;

public interface CreditCheckDao {
    public CheckValidatorEntity getConsultCreditValidation(String ruc, String idCompania, int modo);
}
