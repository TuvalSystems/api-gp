package tuval.api.web.controller.transferencia;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import tuval.api.web.models.Utils.ResponseJsonUtils;
import tuval.api.web.models.dto.tranferencias.TransfersCompartmentsOrderDto;
import tuval.api.web.models.entity.tuval.transferencia.TransferDetailEntity;
import tuval.api.web.models.entity.tuval.transferencia.TransferHeaderEntity;
import tuval.api.web.models.request.bodegas.transferencias.TransferCompartmentOrdersRequest;
import tuval.api.web.models.service.tuval.transferencia.TransferenciaService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TransferenciaController {

    private final TransferenciaService transferenciaService;


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
    @PostMapping("/transferencia-compartimento")
    public ResponseEntity<Map<String, Object>> transferenciaCompartimento( @RequestBody TransferCompartmentOrdersRequest transfers) {
        try {
           TransfersCompartmentsOrderDto result = transferenciaService.transferenciaCompartimentoService(transfers);
            return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", result);
        } catch (Exception e) {
            return ResponseJsonUtils.buildException(e);
        }
    }

}
