package tuval.api.web.bodega.transferencias.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tuval.api.web.common.response.ResponseJsonUtils;
import tuval.api.web.bodega.transferencias.dto.RepositionDetailDto;
import tuval.api.web.bodega.transferencias.dto.RepositionHeaderDto;
import tuval.api.web.bodega.transferencias.dto.TransfersCompartmentsOrderDto;
import tuval.api.web.bodega.transferencias.request.DetailRepositionRequest;
import tuval.api.web.bodega.transferencias.request.HeaderRepositionRequest;
import tuval.api.web.bodega.transferencias.request.TransferCompartmentOrdersRequest;
import tuval.api.web.bodega.transferencias.service.tuval.TransferenciaService;

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
