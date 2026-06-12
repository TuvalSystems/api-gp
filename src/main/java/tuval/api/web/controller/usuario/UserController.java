package tuval.api.web.controller.usuario;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tuval.api.web.models.Utils.ResponseJsonUtils;
import tuval.api.web.models.dto.usuarios.UserEmailDto;
import tuval.api.web.models.entity.dynamics.usuario.UserValidatorEntity;
import tuval.api.web.models.request.usuarios.UserRequest;
import tuval.api.web.models.request.usuarios.UserSearchEmailRequest;
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
  @PostMapping("/validar-usuario")
  public ResponseEntity<Map<String, Object>> validateUser(@RequestBody UserRequest userRequest) {

    try {
      UserValidatorEntity resp = userService.validateUserCredentials(userRequest);
      return ResponseJsonUtils.buildSuccessWithData("Validacion Exitosa", resp);
    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }

  @PostMapping("/buscar-correo-usuario")
  public ResponseEntity<Map<String, Object>> buscarCorreoUsuario(
      @RequestBody UserSearchEmailRequest req) {
    try {
      UserEmailDto resp = userService.findEmailService(req);
      return ResponseJsonUtils.buildSuccessWithData("Correo Obtenido correctamente", resp);
    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }
}
