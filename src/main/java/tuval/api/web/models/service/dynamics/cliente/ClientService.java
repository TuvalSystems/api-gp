package tuval.api.web.models.service.dynamics.cliente;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tuval.api.web.models.dao.dynamics.cliente.ClientDynamicDao;
import tuval.api.web.models.dao.tuval.clientes.ClientTuvalDao;
import tuval.api.web.models.dto.cliente.ListAllSegmentsDto;
import tuval.api.web.models.dto.cliente.ListAllSubsegmentsDto;
import tuval.api.web.models.dto.cliente.ListSegmentClientDto;
import tuval.api.web.models.entity.dynamics.cliente.ListSellerEntity;
import tuval.api.web.models.entity.dynamics.cliente.TypeSellerEntity;
import tuval.api.web.models.request.cliente.ClientRequest;
import tuval.api.web.models.request.cliente.ClientSubsegmentRucRequest;
import tuval.api.web.models.request.cliente.SellerRequest;

@Service
public class ClientService {
    
    @Autowired
    private ClientDynamicDao clientDynamicDao;
        
    @Autowired
    private ClientTuvalDao clientTuvalDao;
    public ListSellerEntity getListSeller(SellerRequest req) {
        // return new ListSellerEntity(coUser, txUsr, coTipven);
        return clientDynamicDao.findByTxUsr(req);
    }


    public List<ListAllSegmentsDto> getAllSegmentos() {
        return clientDynamicDao.findAllSegment();
    }

    public List<ListAllSubsegmentsDto> getAllSubSegmentsBySegment(int coAtrPadre) {
        return clientDynamicDao.findAllSubSegmentsBySegment(coAtrPadre);
    }

    public ListSegmentClientDto getListSegmentos(String tx_cli) {
        return clientDynamicDao.findSegmentByTxCli(tx_cli);
    }

    public TypeSellerEntity getTypeSellerForUserService(String username) {
        return clientDynamicDao.getTypeSellerForTxUsr(username);
    }

    
    public void storeClienteService(ClientRequest request) {
         clientTuvalDao.executeSpCreateOrModifyClient(request);
    }

    public void storeClientSubSegmentAndRucService(ClientSubsegmentRucRequest req) {
        clientDynamicDao.insertClientSubSegmentAndRuc(req);
    }

}
