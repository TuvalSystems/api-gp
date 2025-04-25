package tuval.api.web.models.dao.dynamics.transferencia;

import java.sql.Date;
import java.util.Map;

import org.springframework.http.ResponseEntity;

public interface TransferDynamicDao {
    ResponseEntity<Map<String, Object>> updateTrcNumdocAndTrcStatusTransfer(Date date, String numDoc);

}
