package tuval.api.web.models.service.tuval.admin;

import java.util.List;

import org.springframework.stereotype.Service;

import tuval.api.web.models.dao.tuval.admin.SyncClientsDao;
import tuval.api.web.models.dto.admin.SyncClientsDto;
import tuval.api.web.models.request.admin.SyncClientsRequest;

@Service
public class SyncClientsService {
    
    private final SyncClientsDao syncClientsDao;

    public SyncClientsService(SyncClientsDao syncClientsDao) {
        this.syncClientsDao = syncClientsDao;
    }

    public List<SyncClientsDto> getListSyncClients(SyncClientsRequest request) {
        return syncClientsDao.getListSyncClients(request);
    }
}
