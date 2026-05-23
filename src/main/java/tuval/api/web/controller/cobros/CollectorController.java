package tuval.api.web.controller.cobros;

import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tuval.api.web.models.Utils.ResponseJsonUtils;
import tuval.api.web.models.dto.cobros.BusinessDaysDto;
import tuval.api.web.models.dto.cobros.CheckValidatorDto;
import tuval.api.web.models.dto.cobros.ClientOverdueDocumentDto;
import tuval.api.web.models.dto.cobros.CollectionSummaryDTO;
import tuval.api.web.models.dto.cobros.CustomerCollectionDto;
import tuval.api.web.models.entity.tuval.cobros.ClientSinSoporteEntity;
import tuval.api.web.models.entity.tuval.cobros.ClientStatusAccountResponseEntity;
import tuval.api.web.models.entity.tuval.cobros.ListClientOverdueDocumentEntity;
import tuval.api.web.models.entity.tuval.cobros.ListaClientSinSoporteEntity;
import tuval.api.web.models.request.cobros.CustomerCollectionRequest;
import tuval.api.web.models.service.dynamics.cobros.ReceivablesService;
import tuval.api.web.models.service.tuval.cobros.CreditCheckService;

@RestController
@RequestMapping(value = "/api/cobros", produces = "application/json")
@RequiredArgsConstructor
public class CollectorController {

  private final CreditCheckService creditCheckService;
  private final ReceivablesService receivablesService;

  @GetMapping("/consultar-cupo-disponible-cliente")
  public ResponseEntity<Map<String, Object>> getConsultCreditValidation(
      @RequestParam String ruc,
      @RequestParam(required = false, defaultValue = "") String idCompania) {

    try {

      if (ruc == null || ruc.isEmpty()) {
        return ResponseJsonUtils.buildError("Falta el campo " + "ruc" + "o está vacío.");
      }

      if (idCompania == null || idCompania.isEmpty()) {
        return ResponseJsonUtils.buildError("Falta el campo " + "idCompania" + "o está vacío.");
      }
      CheckValidatorDto result = creditCheckService.getConsultCreditValidation(ruc, idCompania);
      return ResponseJsonUtils.buildSuccessWithData("Datos Obtendidos Correctamente!", result);
    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }

  @GetMapping("/consultar-documentos-vencidos-cliente")
  public ResponseEntity<Map<String, Object>> getConsultaDocumentoVencidosCliente(
      @RequestParam String ruc,
      @RequestParam(required = false, defaultValue = "") String idCompania) {

    try {

      if (ruc == null || ruc.isEmpty()) {
        return ResponseJsonUtils.buildError("Falta el campo " + "ruc" + "o está vacío.");
      }

      if (idCompania == null || idCompania.isEmpty()) {
        return ResponseJsonUtils.buildError("Falta el campo " + "idCompania" + "o está vacío.");
      }
      ClientOverdueDocumentDto result =
          creditCheckService.getConsultExpiredClientDocument(ruc, idCompania);
      return ResponseJsonUtils.buildSuccessWithData("Consulta de Documentos Vencidos", result);
    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }

  @GetMapping("/consultar-lista-documentos-vencidos-cliente")
  public ResponseEntity<Map<String, Object>> getConsultaDocumentoVencidosClienteList(
      @RequestParam String ruc,
      @RequestParam(required = false, defaultValue = "") String idCompania) {

    try {

      if (ruc == null || ruc.isEmpty()) {
        return ResponseJsonUtils.buildError("Falta el campo " + "ruc" + "o está vacío.");
      }

      if (idCompania == null || idCompania.isEmpty()) {
        return ResponseJsonUtils.buildError("Falta el campo " + "idCompania" + "o está vacío.");
      }
      ListClientOverdueDocumentEntity result =
          creditCheckService.getConsultExpiredClientDocumentList(ruc, idCompania);
      return ResponseJsonUtils.buildSuccessWithData(
          "Consulta Lista Documentos Vencidos", result.data);
    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }

  @GetMapping("/consultar-documentos-sin-soporte-cliente")
  public ResponseEntity<Map<String, Object>> getConsultaDocumentoClienteSinSoporte(
      @RequestParam String ruc,
      @RequestParam(required = false, defaultValue = "") String idCompania) {

    try {

      if (ruc == null || ruc.isEmpty()) {
        return ResponseJsonUtils.buildError("Falta el campo " + "ruc" + "o está vacío.");
      }

      if (idCompania == null || idCompania.isEmpty()) {
        return ResponseJsonUtils.buildError("Falta el campo " + "idCompania" + "o está vacío.");
      }
      ClientSinSoporteEntity result =
          creditCheckService.getConsultDocumentClientDontSupport(ruc, idCompania);
      return ResponseJsonUtils.buildSuccessWithData("Consulta Documentos sin Soporte", result);
    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }

  @GetMapping("/consultar-lista-documentos-sin-soporte-cliente")
  public ResponseEntity<Map<String, Object>> getConsultaListaDocumentosClienteSinSoporte(
      @RequestParam String ruc,
      @RequestParam(required = false, defaultValue = "") String idCompania) {

    try {

      if (ruc == null || ruc.isEmpty()) {
        return ResponseJsonUtils.buildError("Falta el campo " + "ruc" + "o está vacío.");
      }

      if (idCompania == null || idCompania.isEmpty()) {
        return ResponseJsonUtils.buildError("Falta el campo " + "idCompania" + "o está vacío.");
      }
      ListaClientSinSoporteEntity result =
          creditCheckService.getConsultDocumentClientDontSupport2(ruc, idCompania);
      return ResponseJsonUtils.buildSuccessWithData(
          "Consulta Lista Documentos sin Soporte", result.data);
    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }

  @GetMapping("/consultar-detalle-estado-cuenta-cliente")
  public ResponseEntity<Map<String, Object>> getConsultaEstadoCuentaCliente(
      @RequestParam(required = true) String ruc, @RequestParam(required = true) String idCompania) {

    try {

      if (ruc == null || ruc.isEmpty()) {
        return ResponseJsonUtils.buildError("Falta el campo " + "ruc" + "o está vacío.");
      }

      if (idCompania == null || idCompania.isEmpty()) {
        return ResponseJsonUtils.buildError("Falta el campo " + "idCompania" + "o está vacío.");
      }
      ClientStatusAccountResponseEntity result =
          creditCheckService.getConsultaEstadoCuentaDetalleCliente(ruc);
      return ResponseJsonUtils.buildSuccessWithData("Detalle Estado de cuenta", result);
    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }

  @GetMapping("/consultar-dias-feriado")
  public ResponseEntity<Map<String, Object>> consultarDiaFeriado(
      @RequestParam(required = true) String fecha,
      @RequestParam(required = true) String idCompany) {
    try {
      BusinessDaysDto result = creditCheckService.getBusinessDaysCheck(fecha);
      return ResponseJsonUtils.buildSuccessWithData("Datos Obtendidos Correctamente", result);
    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }

  @GetMapping("consultar-cobros-sin-aplicar")
  public ResponseEntity<Map<String, Object>> obtenerConsultaCobrosSinAplicar(
      @RequestParam(required = true) String ruc, @RequestParam(required = true) String idCompania) {
    try {
      List<CollectionSummaryDTO> result =
          creditCheckService.getConsultCollectionSummaryService(ruc, idCompania);
      return ResponseJsonUtils.buildSuccessWithData("Datos Obtendidos Correctamente", result);
    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }

  @PostMapping("consulta-aplicaciones-pago")
  public ResponseEntity<Map<String, Object>> obtenerAplicacionesPagoFactura(
      @RequestBody CustomerCollectionRequest payload) {
    try {
      List<CustomerCollectionDto> result =
          receivablesService.getInvoicePaymentApplications(payload);
      return ResponseJsonUtils.buildSuccessWithData("Datos Obtendidos Correctamente", result);
    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }
}
