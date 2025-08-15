package tuval.api.web.models.service.tuval.transacciones;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tuval.api.web.models.dao.tuval.transacciones.transactionDao;
import tuval.api.web.models.entity.tuval.transacciones.reciboCompraL.validateReceidLRequestEntity;
import tuval.api.web.models.entity.tuval.transacciones.reciboCompraL.validateResponseEntity;

@Service
public class transactionService {
    @Autowired

    private transactionDao transactionDao;

    public validateResponseEntity validateReceivedL(validateReceidLRequestEntity request) {
        return transactionDao.executeSpValidateGenerateReceivedItemL(request);
    }


    public validateResponseEntity deleteReceivedL(validateReceidLRequestEntity request) {
        return transactionDao.executeSpDeleteReceivedHeaderAndDetail(request);
    }
}
