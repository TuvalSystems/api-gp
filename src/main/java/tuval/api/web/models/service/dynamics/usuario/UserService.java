package tuval.api.web.models.service.dynamics.usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tuval.api.web.models.dao.dynamics.usuario.UserValidatorDao;
import tuval.api.web.models.dto.usuarios.UserRequest;
import tuval.api.web.models.entity.dynamics.usuario.UserValidatorEntity;

@Service
public class UserService {

    @Autowired
    private UserValidatorDao userValidatorDao;

    public UserValidatorEntity validateUserCredentials(UserRequest userRequest) {

        return userValidatorDao.validateUserDynamic(userRequest);
    }
}
