package tuval.api.web.models.dto.usuarios;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {

  private String idCompany;
  private String username;
  private String password;


}
