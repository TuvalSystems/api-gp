package tuval.api.web.controller.transferencia;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tuval.api.web.models.entity.transferencia.TransferDetailEntity;
import tuval.api.web.models.entity.transferencia.TransferHeaderEntity;
import tuval.api.web.models.service.transferencia.TransferenciaService;

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

}
