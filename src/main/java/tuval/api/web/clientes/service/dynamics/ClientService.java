package tuval.api.web.clientes.service.dynamics;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import tuval.api.web.clientes.dao.dynamics.ClientDynamicDao;
import tuval.api.web.clientes.dao.tuval.ClientTuvalDao;
import tuval.api.web.clientes.dto.ListAllSegmentsDto;
import tuval.api.web.clientes.dto.ListAllSubsegmentsDto;
import tuval.api.web.clientes.dto.ListSegmentClientDto;
import tuval.api.web.clientes.dto.ListSellerDto;
import tuval.api.web.clientes.dto.NewAddressStatusDto;
import tuval.api.web.clientes.dto.TypeSellerDto;
import tuval.api.web.clientes.request.ClientBranchRequets;
import tuval.api.web.clientes.request.BranchConfigSyncKeyRequest;
import tuval.api.web.clientes.request.ClientRequest;
import tuval.api.web.clientes.request.ClientSubsegmentRucRequest;
import tuval.api.web.clientes.request.NewAddressRequest;
import tuval.api.web.clientes.request.SellerRequest;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientDynamicDao clientDynamicDao;
    private final  ClientTuvalDao clientTuvalDao;
    
    public List<tuval.api.web.clientes.dto.PendingBranchConfigSyncDto> getPendingBranchConfigurations(
            tuval.api.web.clientes.request.PendingBranchConfigSyncRequest request) {
        return clientDynamicDao.findPendingBranchConfigurations(request.getLimit(), request.getCursor());
    }

    public int markBranchConfigurationSynced(BranchConfigSyncKeyRequest request) {
        return clientDynamicDao.markBranchConfigurationSynced(request);
    }

    @org.springframework.transaction.annotation.Transactional(transactionManager = "dynamicsTransactionManager")
    public int confirmBranchConfigurationSync(BranchConfigSyncKeyRequest request) {
        int updated = clientDynamicDao.markBranchConfigurationSynced(request);
        int inserted = clientDynamicDao.insertBranchConfigurationHistory(request);
        if (updated != 1 || inserted != 1) {
            throw new IllegalStateException("Se esperaba una sola configuracion de sucursal");
        }
        return inserted;
    }

    public int saveBranchConfigurationHistory(BranchConfigSyncKeyRequest request) {
        return clientDynamicDao.insertBranchConfigurationHistory(request);
    }

    public long getClientBranchesPendingMySqlSyncCount() {
        return clientDynamicDao.countClientBranchesPendingMySqlSync();
    }

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
