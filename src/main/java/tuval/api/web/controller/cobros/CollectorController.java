package tuval.api.web.controller.cobros;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tuval.api.web.models.Utils.ResponseJsonUtils;
import tuval.api.web.models.entity.tuval.cobros.CheckValidatorEntity;
import tuval.api.web.models.entity.tuval.cobros.ClientOverdueDocumentEntity;
import tuval.api.web.models.entity.tuval.cobros.ClientSinSoporteEntity;
import tuval.api.web.models.entity.tuval.cobros.ClientStatusAccountResponseEntity;
import tuval.api.web.models.entity.tuval.cobros.ListClientOverdueDocumentEntity;
import tuval.api.web.models.entity.tuval.cobros.ListaClientSinSoporteEntity;
import tuval.api.web.models.service.tuval.cobros.CreditCheckService;

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

     @GetMapping("/consultar-documentos-vencidos-cliente")
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

        @GetMapping("/consultar-lista-documentos-vencidos-cliente")
    public ResponseEntity<Map<String, Object>> getConsultaDocumentoVencidosClienteList(@RequestParam String ruc,
            @RequestParam(required = false, defaultValue = "") String idCompania) {

        try {

            if (ruc == null || ruc.isEmpty()) {
                return ResponseJsonUtils.buildError("Falta el campo " + "ruc" + "o está vacío.");
            }

            if (idCompania == null || idCompania.isEmpty()) {
                return ResponseJsonUtils.buildError("Falta el campo " + "idCompania" + "o está vacío.");

                
            }
            ListClientOverdueDocumentEntity result = creditCheckService.getConsultExpiredClientDocumentList(ruc, idCompania);
            return ResponseJsonUtils.buildSuccessWithData("Datos Obtendidos Correctamente", result.data);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseJsonUtils.buildException(e);

        }

    }



    @GetMapping("/consultar-documentos-sin-soporte-cliente")
    public ResponseEntity<Map<String, Object>> getConsultaDocumentoClienteSinSoporte(@RequestParam String ruc,
            @RequestParam(required = false, defaultValue = "") String idCompania) {

        try {

            if (ruc == null || ruc.isEmpty()) {
                return ResponseJsonUtils.buildError("Falta el campo " + "ruc" + "o está vacío.");
            }

            if (idCompania == null || idCompania.isEmpty()) {
                return ResponseJsonUtils.buildError("Falta el campo " + "idCompania" + "o está vacío.");

            }
            ClientSinSoporteEntity result = creditCheckService.getConsultDocumentClientDontSupport(ruc, idCompania);
            return ResponseJsonUtils.buildSuccessWithData("Datos Obtendidos Correctamente", result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseJsonUtils.buildException(e);

        }

    }

    
    @GetMapping("/consultar-lista-documentos-sin-soporte-cliente")
    public ResponseEntity<Map<String, Object>> getConsultaDocumentoClienteSinSoporte2(@RequestParam String ruc,
            @RequestParam(required = false, defaultValue = "") String idCompania) {

        try {

            if (ruc == null || ruc.isEmpty()) {
                return ResponseJsonUtils.buildError("Falta el campo " + "ruc" + "o está vacío.");
            }

            if (idCompania == null || idCompania.isEmpty()) {
                return ResponseJsonUtils.buildError("Falta el campo " + "idCompania" + "o está vacío.");

            }
            ListaClientSinSoporteEntity result = creditCheckService.getConsultDocumentClientDontSupport2(ruc, idCompania);
            return ResponseJsonUtils.buildSuccessWithData("Datos Obtendidos Correctamente", result.data);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseJsonUtils.buildException(e);

        }

    }

    @GetMapping("/consultar-detalle-estado-cuenta-cliente")
    public ResponseEntity<Map<String, Object>> getConsultaEstadoCuentaProveedor(@RequestParam(required = true) String ruc,
            @RequestParam(required = true) String idCompania) {

        try {

            if (ruc == null || ruc.isEmpty()) {
                return ResponseJsonUtils.buildError("Falta el campo " + "ruc" + "o está vacío.");
            }

            if (idCompania == null || idCompania.isEmpty()) {
                return ResponseJsonUtils.buildError("Falta el campo " + "idCompania" + "o está vacío.");

            }
            ClientStatusAccountResponseEntity result = creditCheckService.getConsultaEstadoCuentaDetalleCliente(ruc);
            return ResponseJsonUtils.buildSuccessWithData("Datos Obtendidos Correctamente", result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseJsonUtils.buildException(e);

        }

    }




}
