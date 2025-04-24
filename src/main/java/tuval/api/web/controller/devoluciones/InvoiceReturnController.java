package tuval.api.web.controller.devoluciones;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import tuval.api.web.models.service.tuval.devoluciones.InvoiceReturnService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping(value = "/api" , produces = "application/json")
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
                    "message", "No se puede procesar un documento con 2 o más cabeceras."
                ));
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

}
