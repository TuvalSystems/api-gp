package tuval.api.web.models.service.tuval.admin;

import java.util.List;

import org.springframework.stereotype.Service;

import tuval.api.web.models.dao.tuval.admin.SyncClientsDao;
import tuval.api.web.models.dto.admin.SyncClientBranchesDto;
import tuval.api.web.models.dto.admin.SyncClientCoordinatesDto;
import tuval.api.web.models.dto.admin.SyncClientsDto;
import tuval.api.web.models.request.admin.SyncClientsRequest;

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
