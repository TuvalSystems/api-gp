package tuval.api.web.models.dao.tuval.transferencia;

import java.sql.Date;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import tuval.api.web.models.entity.tuval.transferencia.TransferDetailResponseEntity;
import tuval.api.web.models.entity.tuval.transferencia.TransferHeaderResponseEntity;

public interface TransferDao {

    TransferHeaderResponseEntity consultDocumentTransfer(Date date, String userid, int Modo, String numDoc);
    TransferDetailResponseEntity consultarDocumentodetalleTransferencia(String userid, int Modo, String numDoc);
     ResponseEntity<Map<String, Object>> TransferenciaCompartimento(  Date fecha, String item, String bodega, Double cantidad, String BinOrigen, String BinDestino);
} 
