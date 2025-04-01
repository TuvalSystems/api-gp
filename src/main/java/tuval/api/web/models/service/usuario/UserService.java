package tuval.api.web.models.service.usuario;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import tuval.api.web.models.dao.usuario.UserValidatorDao;
import tuval.api.web.models.entity.usuario.UserValidatorResponseEntity;

@Service
public class UserService {

    @Autowired
    private UserValidatorDao userValidatorDao;

    public ResponseEntity<Map<String, Object>> validateUserCredentials(String idCompany, String username) {

        Map<String, Object> response = new HashMap<>();

        // Validate input parameters
        if (idCompany == null || idCompany.isEmpty() || username == null || username.isEmpty()) {
            response.put("codret", 99);
            response.put("msj", "Parámetros inválidos");
            response.put("userid", null);
            response.put("interId", null);
            return ResponseEntity.badRequest().body(response);
        }

        UserValidatorResponseEntity result = userValidatorDao.validateUserDynamic(idCompany, username);
      
        try {
            System.out.println("Result: " + result);
            if (result == null || result.getData() == null || result.getData().getUserId() == null) {
                response.put("codret", 99);
                response.put("msj", "Usuario no encontrado");
                response.put("userid", null);
                response.put("interId", null);

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            response.put("codret", result.getCodRet());
            response.put("msj", result.getMsj());
            response.put("userid", result.getData().getUserId());
            response.put("interId", result.getData().getInterId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error en servicio: " + e);
            response.put("codret", 99);
            response.put("msj", "Error interno del servidor");
            response.put("userid", null);
            response.put("interId", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

    }
}
