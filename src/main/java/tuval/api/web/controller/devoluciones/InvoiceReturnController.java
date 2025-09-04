package tuval.api.web.controller.devoluciones;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;

import com.fasterxml.jackson.core.type.TypeReference;

import tuval.api.web.models.Utils.ResponseJsonUtils;
import tuval.api.web.models.dto.devolucion.DevolutionRequest;
import tuval.api.web.models.entity.tuval.devoluciones.DevolutionResponse;
import tuval.api.web.models.entity.tuval.devoluciones.ItemsDevolucionEntity;
import tuval.api.web.models.entity.tuval.devoluciones.RetornoEntity;
import tuval.api.web.models.service.tuval.devoluciones.InvoiceReturnService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping(value = "/api", produces = "application/json")
public class InvoiceReturnController {

    @Autowired
    private InvoiceReturnService invoiceReturnService;

    @PostMapping("/devoluciones/generar-nota-credito")
    public ResponseEntity<?> processNotaCredito(@RequestBody JsonNode devolucion) {
        Map<String, Object> response = new HashMap<>();
        try {
            JsonNode cabeceraNode = devolucion.get("cabecera");
            JsonNode detalleNode = devolucion.get("detalle");
            if (cabeceraNode == null || !cabeceraNode.isArray() || cabeceraNode.size() == 0) {
                response.put("success", false);
                response.put("message", "Falta el campo 'cabecera' o está vacío.");
                return ResponseEntity.badRequest().body(response);
            }

            if (detalleNode == null || !detalleNode.isArray() || detalleNode.size() == 0) {
                response.put("success", false);
                response.put("message", "Falta el campo 'detalle' o está vacío.");
                return ResponseEntity.badRequest().body(response);
            }
            if (cabeceraNode.size() > 1) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "No se puede procesar un documento con 2 o más cabeceras."));
            }

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> cabecera = mapper.convertValue(
                    cabeceraNode.get(0), new TypeReference<Map<String, Object>>() {
                    });
            List<Map<String, Object>> detalleList = mapper.convertValue(
                    detalleNode, new TypeReference<List<Map<String, Object>>>() {
                    });

            return invoiceReturnService.generacionNotaCreditoVTA(cabecera, detalleList);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "Error de formato en los datos enviados.");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error interno al procesar la solicitud.");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/devoluciones/respuesta-nota-credito")
    public ResponseEntity<?> respuestaNotaCredito(@RequestParam String document,
            @RequestParam String tipo) {
        return invoiceReturnService.getResponseCreditNote(document, tipo);
    }

    @PostMapping("/devoluciones/generar-devolucion-venta")
    public ResponseEntity<Map<String, Object>> generarAjusteDevolucion(@Valid @RequestBody DevolutionRequest data) {
        try {
            DevolutionResponse response = invoiceReturnService.generateAjustmentDevolutionService(data);
            return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", response);
        } catch (Exception e) {
           e.printStackTrace(); // para ver la traza completa
            return ResponseJsonUtils.buildException(e);
        }
    }



    @GetMapping("/devoluciones/consulta-item-compartimento-bin")
    public ResponseEntity<Map<String, Object>> consultarItemCompartimento(
            @RequestParam String item,
            @RequestParam String bodega) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (item == null || item.isEmpty()) {
                response.put("success", false);
                response.put("message", "Falta el campo 'detalle' o está vacío.");
                return ResponseEntity.badRequest().body(response);
            }

            if (bodega == null || bodega.isEmpty()) {
                response.put("success", false);
                response.put("message", "Falta el campo 'bodega' o está vacío.");
                return ResponseEntity.badRequest().body(response);
            }

            // ✅ Llamada al servicio para consultar el item en el compartimento
            return invoiceReturnService.getResponseItemBin(item, bodega);
        } catch (Exception e) {
            // 🔄 Manejo de excepciones y respuesta clara
            // Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error interno al procesar la solicitud.");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/devoluciones/consultar-grupo-economico-cliente")
    public ResponseEntity<Map<String, Object>> getClientGroupEconomic(
            @RequestParam String ruc) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (ruc == null || ruc.isEmpty()) {
                response.put("success", false);
                response.put("message", "Falta el campo 'ruc' o está vacío.");
                return ResponseEntity.badRequest().body(response);
            }
            return invoiceReturnService.getClientGroupEconomicService(ruc);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error interno al procesar la solicitud.");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

    }

}
