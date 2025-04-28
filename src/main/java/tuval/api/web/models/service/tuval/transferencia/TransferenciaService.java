package tuval.api.web.models.service.tuval.transferencia;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import tuval.api.web.models.entity.tuval.transferencia.TransferDetailEntity;
import tuval.api.web.models.entity.tuval.transferencia.TransferHeaderEntity;

public interface TransferenciaService {

    List<TransferHeaderEntity> consultaCabeceraTransferencia(String idCompany, String fecha, String numDoc);
    List<TransferDetailEntity> consultarDetalleTransferencia(String idCompany, String numDoc);
     ResponseEntity<Map<String, Object>> transferenciaCompartimentoService(String fecha, String item, String bodega, Double cantidad, String BinOrigen, String BinDestino);
   
}
