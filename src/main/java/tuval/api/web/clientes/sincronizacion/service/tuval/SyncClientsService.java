package tuval.api.web.clientes.sincronizacion.service.tuval;

import java.util.List;

import org.springframework.stereotype.Service;

import tuval.api.web.clientes.sincronizacion.dao.tuval.SyncClientsDao;
import tuval.api.web.clientes.sincronizacion.dto.SyncClientBranchesDto;
import tuval.api.web.clientes.sincronizacion.dto.SyncClientCoordinatesDto;
import tuval.api.web.clientes.sincronizacion.dto.SyncClientsDto;
import tuval.api.web.clientes.sincronizacion.request.SyncClientsRequest;

@Service
public class SyncClientsService {

    private final SyncClientsDao syncClientsDao;

    public SyncClientsService(SyncClientsDao syncClientsDao) {
        this.syncClientsDao = syncClientsDao;
    }

    public List<SyncClientsDto> getListSyncClients(SyncClientsRequest request) {
        return syncClientsDao.executeSpListSyncClients(request);
    }

    public List<SyncClientBranchesDto> getListClientsBranchesService(SyncClientsRequest request) {
        return syncClientsDao.executeSpListClientsBranchesDao(request);
    }

    public List<SyncClientCoordinatesDto> getListClientCoordinatesService() {
        return syncClientsDao.executeSpListClientCoordinates();
    }

}
