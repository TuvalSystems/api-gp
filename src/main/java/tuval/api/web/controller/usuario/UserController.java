package tuval.api.web.controller.usuario;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tuval.api.web.models.service.dynamics.usuario.UserService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @CrossOrigin
    @GetMapping("/validar-usuario")
    public ResponseEntity<?> validateUser(@RequestParam String idCompany, @RequestParam String username) {

        if (idCompany == null || idCompany.isEmpty() || username == null || username.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "valid", false,
                    "message", "Company ID y username son requeridos"));
        }
        try {
            Map<String, Object> serviceResponse = userService.validateUserCredentials(idCompany, username).getBody();
            return ResponseEntity.ok(serviceResponse);

        } catch (Exception e) {

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("codret", 99);
            errorResponse.put("msj", "Error en el servidor");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);

        }
    }
}
