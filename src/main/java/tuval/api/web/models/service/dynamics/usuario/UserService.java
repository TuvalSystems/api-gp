package tuval.api.web.models.service.dynamics.usuario;

import org.springframework.stereotype.Service;

import tuval.api.web.models.dao.dynamics.usuario.UserDynamicsDao;
import tuval.api.web.models.dto.usuarios.UserEmailDto;
import tuval.api.web.models.entity.dynamics.usuario.UserValidatorEntity;
import tuval.api.web.models.request.usuarios.UserRequest;
import tuval.api.web.models.request.usuarios.UserSearchEmailRequest;

@Service
public class UserService {

    private final UserDynamicsDao userDynamicDao;

    public UserService(UserDynamicsDao userDynamicDao){
            this.userDynamicDao = userDynamicDao;
    }

    public UserValidatorEntity validateUserCredentials(UserRequest req) {
        return userDynamicDao.validateUserDynamic(req);
    }

    public UserEmailDto findEmailService(UserSearchEmailRequest req){
            return userDynamicDao.findUserEmail(req);
    }


}
