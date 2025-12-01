package tuval.api.web.models.service.dynamics.cliente;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tuval.api.web.models.dao.dynamics.cliente.ClientDynamicDao;
import tuval.api.web.models.dto.cliente.SellerRequest;
import tuval.api.web.models.entity.dynamics.cliente.ListSegmentClientEntity;
import tuval.api.web.models.entity.dynamics.cliente.ListSellerEntity;
import tuval.api.web.models.entity.dynamics.cliente.TypeSellerEntity;

@Service
public class ClientService {
    
    @Autowired
    private ClientDynamicDao clientDynamicDao;
    public ListSellerEntity getListSeller(SellerRequest req) {
        // return new ListSellerEntity(coUser, txUsr, coTipven);
        return clientDynamicDao.findByTxUsr(req);
    }

    public ListSegmentClientEntity getListSegmentos(String tx_cli) {
        return clientDynamicDao.findSegmentByTxCli(tx_cli);
    }

    public TypeSellerEntity getTypeSellerForUserService(String username) {
        return clientDynamicDao.getTypeSellerForTxUsr(username);
    }

}
