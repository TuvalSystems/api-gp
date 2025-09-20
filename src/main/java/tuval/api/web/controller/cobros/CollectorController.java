package tuval.api.web.controller.cobros;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tuval.api.web.models.Utils.ResponseJsonUtils;
import tuval.api.web.models.entity.tuval.cobros.CheckValidatorEntity;
import tuval.api.web.models.entity.tuval.cobros.ClientOverdueDocumentEntity;
import tuval.api.web.models.entity.tuval.cobros.pruebaEntity;
import tuval.api.web.models.service.tuval.cobros.CreditCheckService;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping(value = "/api/cobros", produces = "application/json")
public class CollectorController {

    @Autowired
    private CreditCheckService creditCheckService;

    @GetMapping("/consultar-cupo-disponible-cliente")
    public ResponseEntity<Map<String, Object>> getConsultCreditValidation(@RequestParam String ruc,
            @RequestParam(required = false, defaultValue = "") String idCompania) {

        try {

            if (ruc == null || ruc.isEmpty()) {
                return ResponseJsonUtils.buildError("Falta el campo " + "ruc" + "o está vacío.");
            }

            if (idCompania == null || idCompania.isEmpty()) {
                return ResponseJsonUtils.buildError("Falta el campo " + "idCompania" + "o está vacío.");

            }
            CheckValidatorEntity result = creditCheckService.getConsultCreditValidation(ruc, idCompania);
            return ResponseJsonUtils.buildSuccessWithData("Datos Obtendidos Correctamente", result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseJsonUtils.buildException(e);

        }
    }

    @GetMapping("/consultar-ciente-documentos-vencidos")
    public ResponseEntity<Map<String, Object>> getConsultaDocumentoVencidosCliente(@RequestParam String ruc,
            @RequestParam(required = false, defaultValue = "") String idCompania) {

        try {

            if (ruc == null || ruc.isEmpty()) {
                return ResponseJsonUtils.buildError("Falta el campo " + "ruc" + "o está vacío.");
            }

            if (idCompania == null || idCompania.isEmpty()) {
                return ResponseJsonUtils.buildError("Falta el campo " + "idCompania" + "o está vacío.");

            }
            ClientOverdueDocumentEntity result = creditCheckService.getConsultExpiredClientDocument(ruc, idCompania);
            return ResponseJsonUtils.buildSuccessWithData("Datos Obtendidos Correctamente", result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseJsonUtils.buildException(e);

        }

    }

    @GetMapping("/prueba")
    public ResponseEntity<Map<String, Object>> getConsultaDocumentoVencidosCliente() {

        try {
            pruebaEntity result = creditCheckService.funcionprueba();
            return ResponseJsonUtils.buildSuccessWithData("Datos Obtendidos Correctamente", result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseJsonUtils.buildException(e);

        }

    }


    

}
