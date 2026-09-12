package tuval.api.web.usuarios.service.dynamics;

import org.springframework.stereotype.Service;

import tuval.api.web.usuarios.dao.dynamics.UserDynamicsDao;
import tuval.api.web.usuarios.dto.UserEmailDto;
import tuval.api.web.usuarios.entity.dynamics.UserValidatorEntity;
import tuval.api.web.usuarios.request.UserRequest;
import tuval.api.web.usuarios.request.UserSearchEmailRequest;

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
