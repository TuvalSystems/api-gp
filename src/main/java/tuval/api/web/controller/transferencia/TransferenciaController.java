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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tuval.api.web.models.Utils.ResponseJsonUtils;
import tuval.api.web.models.dto.tranferencias.RepositionDetailDto;
import tuval.api.web.models.dto.tranferencias.RepositionHeaderDto;
import tuval.api.web.models.dto.tranferencias.TransfersCompartmentsOrderDto;
import tuval.api.web.models.request.bodegas.reposiciones.DetailRepositionRequest;
import tuval.api.web.models.request.bodegas.reposiciones.HeaderRepositionRequest;
import tuval.api.web.models.request.bodegas.transferencias.TransferCompartmentOrdersRequest;
import tuval.api.web.models.service.tuval.transferencia.TransferenciaService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TransferenciaController {

    private final TransferenciaService transferenciaService;

    @CrossOrigin
    @PostMapping("/consultar-cabecera-transferencia")
    public ResponseEntity<Map<String, Object>> consultarTransferencias(
            @Valid @RequestBody HeaderRepositionRequest req) {
        try {
            List<RepositionHeaderDto> response = transferenciaService.consultaCabeceraTransferencia(req);
            return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", response);
        } catch (Exception e) {
            return ResponseJsonUtils.buildException(e);
        }
    }

    @CrossOrigin
    @PostMapping("/consultar-detalle-transferencia")
    public ResponseEntity<Map<String, Object>> consultarDetalleTransferencia(@Valid @RequestBody DetailRepositionRequest req) {
           try {
            List<RepositionDetailDto> response = transferenciaService.consultarDetalleTransferencia(req);
            return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", response);
        } catch (Exception e) {
            return ResponseJsonUtils.buildException(e);
        }

    }

    @CrossOrigin
    @PostMapping("/transferencia-compartimento")
    public ResponseEntity<Map<String, Object>> transferenciaCompartimento(
            @RequestBody TransferCompartmentOrdersRequest transfers) {
        try {
            TransfersCompartmentsOrderDto result = transferenciaService.transferenciaCompartimentoService(transfers);
            return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", result);
        } catch (Exception e) {
            return ResponseJsonUtils.buildException(e);
        }
    }

}
