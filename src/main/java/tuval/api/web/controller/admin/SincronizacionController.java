package tuval.api.web.controller.admin;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import tuval.api.web.models.service.tuval.admin.SyncClientsService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import tuval.api.web.models.Utils.ResponseJsonUtils;
import tuval.api.web.models.dto.admin.SyncClientsDto;
import tuval.api.web.models.request.admin.SyncClientsRequest;

@RestController
@RequestMapping("/api/sync")
public class SincronizacionController {

    private final SyncClientsService syncClientsService;

    public SincronizacionController(SyncClientsService syncClientsService) {
        this.syncClientsService = syncClientsService;
    }

    @CrossOrigin
    @PostMapping("clientes")
    public ResponseEntity<Map<String, Object>> postMethodName(@RequestBody SyncClientsRequest req) {
        try {
            List<SyncClientsDto> response = syncClientsService.getListSyncClients(req);
            return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", response);
        } catch (Exception e) {
            return ResponseJsonUtils.buildException(e);
        }
    }

}
