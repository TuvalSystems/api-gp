package tuval.api.web.controller.transferencia;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tuval.api.web.models.entity.tuval.transferencia.TransferDetailEntity;
import tuval.api.web.models.entity.tuval.transferencia.TransferHeaderEntity;
import tuval.api.web.models.service.tuval.transferencia.TransferenciaService;

@RestController
@RequestMapping("/api")
public class TransferenciaController {

    @Autowired
    private TransferenciaService transferenciaService;

    @CrossOrigin
    @GetMapping("/consultar-cabecera-transferencia")
    public List<TransferHeaderEntity> consultarTransferencias(@RequestParam String idCompany,
            @RequestParam String fecha,
            @RequestParam(required = false) String numDoc) {
        List<TransferHeaderEntity> response = transferenciaService.consultaCabeceraTransferencia(idCompany, fecha,
                numDoc);
        return response;
    }

    @CrossOrigin
    @GetMapping("/consultar-detalle-transferencia")
    public List<TransferDetailEntity> consultarDetalleTransferencia(@RequestParam String idCompany,
            @RequestParam String numDoc) {
        List<TransferDetailEntity> response = transferenciaService.consultarDetalleTransferencia(idCompany,
                numDoc);
        return response;

    }

    @CrossOrigin
    @GetMapping("/transferencia-compartimento")
    public ResponseEntity<Map<String, Object>> transferenciaCompartimento(@RequestParam String fecha,
            @RequestParam String item, @RequestParam String bodega, @RequestParam Double cantidad,
            @RequestParam String BinOrigen, @RequestParam String BinDestino) {
        Map<String, Object> response = new HashMap<>();

        try {
            ResponseEntity<Map<String, Object>> result = transferenciaService.transferenciaCompartimentoService(fecha,
                    item, bodega, cantidad,
                    BinOrigen, BinDestino);
            Map<String, Object> resultBody = result.getBody();
         
            response.put("success", true);
            response.put("message", "Transferencia procesada correctamente.");
            response.put("data", resultBody != null ? resultBody.get("data") : null);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("data", null);

            return ResponseEntity.badRequest().body(response);
        }
    }

}
