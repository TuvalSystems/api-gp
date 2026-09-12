package tuval.api.web.clientes.service.dynamics;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tuval.api.web.clientes.dao.dynamics.ClientDynamicDao;
import tuval.api.web.clientes.dao.tuval.ClientTuvalDao;
import tuval.api.web.clientes.dto.ListAllSegmentsDto;
import tuval.api.web.clientes.dto.ListAllSubsegmentsDto;
import tuval.api.web.clientes.dto.ListSegmentClientDto;
import tuval.api.web.clientes.dto.ListSellerDto;
import tuval.api.web.clientes.dto.NewAddressStatusDto;
import tuval.api.web.clientes.dto.TypeSellerDto;
import tuval.api.web.clientes.request.ClientBranchRequets;
import tuval.api.web.clientes.request.ClientRequest;
import tuval.api.web.clientes.request.ClientSubsegmentRucRequest;
import tuval.api.web.clientes.request.NewAddressRequest;
import tuval.api.web.clientes.request.SellerRequest;

@Service
public class ClientService {
    
    @Autowired
    private ClientDynamicDao clientDynamicDao;
        
    @Autowired
    private ClientTuvalDao clientTuvalDao;
    public ListSellerDto getListSeller(SellerRequest req) {
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

    public TypeSellerDto getTypeSellerForUserService(String username) {
        return clientDynamicDao.getTypeSellerForTxUsr(username);
    }

    
    public void storeClienteService(ClientRequest request) {
         clientTuvalDao.executeSpCreateOrModifyClient(request);
    }

    public void storeClientSubSegmentAndRucService(ClientSubsegmentRucRequest req) {
        clientDynamicDao.insertClientSubSegmentAndRuc(req);
    }

    public NewAddressStatusDto storeNewAddressService(NewAddressRequest request) {
        return clientTuvalDao.executeSpCreateOrModifyAddress(request);
    }

    public void updateStatusClientBranchService(ClientBranchRequets request) {
        clientDynamicDao.executeSpUpdateStatusBranchClient(request);
    }

}
