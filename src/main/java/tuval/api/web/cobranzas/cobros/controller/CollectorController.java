package tuval.api.web.cobranzas.cobros.controller;

import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;
import tuval.api.web.cobranzas.cobros.request.InvoicePaymentConditionRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import tuval.api.web.cobranzas.cobros.dto.BusinessDaysDto;
import tuval.api.web.cobranzas.cobros.dto.CheckValidatorDto;
import tuval.api.web.cobranzas.cobros.dto.ClientOverdueDocumentDto;
import tuval.api.web.cobranzas.cobros.dto.CustomerCollectionDto;
import tuval.api.web.cobranzas.cobros.dto.CustomerInvoiceCheckReceiptDto;
import tuval.api.web.cobranzas.cobros.dto.CustomerInvoicePendingToPostDto;
import tuval.api.web.cobranzas.cobros.dto.CustomerPendingApplicationDocumentDto;
import tuval.api.web.cobranzas.cobros.dto.CustomerResumenAccountDto;
import tuval.api.web.cobranzas.cobros.dto.CustomerStatusAccountDto;
import tuval.api.web.cobranzas.cobros.entity.tuval.ClientSinSoporteEntity;
import tuval.api.web.cobranzas.cobros.entity.tuval.ClientStatusAccountResponseEntity;
import tuval.api.web.cobranzas.cobros.entity.tuval.ListClientOverdueDocumentEntity;
import tuval.api.web.cobranzas.cobros.entity.tuval.ListaClientSinSoporteEntity;
import tuval.api.web.cobranzas.cobros.request.CustomerApplicationPendingRequest;
import tuval.api.web.cobranzas.cobros.request.CustomerCollectionRequest;
import tuval.api.web.cobranzas.cobros.service.dynamics.ReceivablesService;
import tuval.api.web.cobranzas.cobros.service.tuval.CreditCheckService;
import tuval.api.web.common.response.ResponseJsonUtils;

@RestController
@RequestMapping(value = "/api/cobros", produces = "application/json")
@RequiredArgsConstructor
public class CollectorController {
  @PostMapping("validar-liberacion-despacho")
  public ResponseEntity<Map<String, Object>> validarLiberacionDespacho(
      @Valid @RequestBody InvoicePaymentConditionRequest payload) {
    try {
      Integer codigo = receivablesService.checkMaterialRelease(
          payload.getFactura(), payload.getCondicionPago());
      return ResponseJsonUtils.buildSuccessWithData(
          "Consulta de liberacion de material realizada correctamente", Map.of("codigo", codigo));
    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }


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

  @GetMapping("consultar-documentos-pendientes-aplicacion")
  public ResponseEntity<Map<String, Object>> obtenerDocumentosPendientesAplicacion(
      @RequestParam String ruc) {
    try {
      List<CustomerPendingApplicationDocumentDto> result =
          creditCheckService.getPendingApplicationDocuments(ruc);

      return ResponseJsonUtils.buildSuccessWithData(
          "Documentos pendientes de aplicación obtenidos correctamente", result);
    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }

  @PostMapping("consulta-documentos-cobros-aplicados")
  public ResponseEntity<Map<String, Object>> obtenerAplicacionesPagoFactura(
      @RequestBody CustomerCollectionRequest payload) {
    try {
      List<CustomerCollectionDto> result =
          receivablesService.getInvoicePaymentApplications(payload);
      return ResponseJsonUtils.buildSuccessWithData(
          "Lista de Documentos Obtenidos Correctamente", result);
    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }

  @PostMapping("consulta-recibos-cheques")
  public ResponseEntity<Map<String, Object>> obtenerRecibosCheques(
      @RequestBody CustomerCollectionRequest payload) {
    try {
      List<CustomerInvoiceCheckReceiptDto> result =
          receivablesService.getCheckInvoicePayments(payload);
      return ResponseJsonUtils.buildSuccessWithData("Datos Obtendidos Correctamente", result);
    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }

  @PostMapping("consulta-cheques-no-cobrados")
  public ResponseEntity<Map<String, Object>> obtenerChequesNoCobrados(
      @RequestBody CustomerCollectionRequest payload) {
    try {
      List<CustomerInvoiceCheckReceiptDto> result =
          receivablesService.getCheckInvoicePendingPayments(payload);
      return ResponseJsonUtils.buildSuccessWithData("Datos Obtendidos Correctamente", result);
    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }

  @PostMapping("consulta-aplicaciones-pendientes-por-documento")
  public ResponseEntity<Map<String, Object>> obtenerAplicacionPendientePorDocumento(
      @RequestBody CustomerApplicationPendingRequest req) {
    try {
      CustomerPendingApplicationDocumentDto response =
          receivablesService.getPendingApplicationDocuments(req.getDocumento(), req.getRuc());
      return ResponseJsonUtils.buildSuccessWithData("Aplicacion obtenidas correctamente", response);
    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }

  @GetMapping("consultar-estado-cuenta-cliente")
  public ResponseEntity<Map<String, Object>> obtenerEstadoCuentaCliente(@RequestParam String ruc) {
    try {
      List<CustomerStatusAccountDto> result = creditCheckService.getCustomerStatusAccount(ruc);
      return ResponseJsonUtils.buildSuccessWithData("Estado cuenta obtenido correctamente", result);
    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }

  @GetMapping("consultar-estado-cuenta-cliente-resumen")
  public ResponseEntity<Map<String, Object>> obtenerEstadoCuentaResument(@RequestParam String ruc) {
    try {
      CustomerResumenAccountDto result = creditCheckService.getCustomerResumenAccount(ruc);
      return ResponseJsonUtils.buildSuccessWithData("Estado cuenta obtenido correctamente", result);
    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }



    @PostMapping("consultar-documentos-pendientes-contabilizar")
  public ResponseEntity<Map<String, Object>> obtenerDocumentosPendienteContabilizar(
      @RequestBody CustomerCollectionRequest payload) {
    try {
      List<CustomerInvoicePendingToPostDto> result =
          receivablesService.getPendingPostingDocuments(payload);
      return ResponseJsonUtils.buildSuccessWithData("Datos Obtendidos Correctamente", result);
    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }

  
}
