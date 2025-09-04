package tuval.api.web.controller.cliente;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import tuval.api.web.models.Utils.ResponseJsonUtils;
import tuval.api.web.models.dto.cliente.SellerRequest;
import tuval.api.web.models.entity.dynamics.cliente.ListSellerEntity;
import tuval.api.web.models.service.dynamics.cliente.ClientService;

@RestController
@RequestMapping(value = "/api/cliente", produces = "application/json")
public class ClienteController {

    @Autowired
    private ClientService clienteService;

    @CrossOrigin
    @PostMapping("/obtener-tipo-vendedor")
    public ResponseEntity<Map<String, Object>> getSellerListClient(@Valid @RequestBody SellerRequest req) {
        try {
            ListSellerEntity response = clienteService.getListSeller(req);
            return ResponseJsonUtils.buildSuccessWithData("Respuesta exitosa", response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseJsonUtils.buildException(e);
        }

    }
}
