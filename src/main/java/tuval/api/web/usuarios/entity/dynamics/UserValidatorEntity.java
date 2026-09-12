package tuval.api.web.usuarios.entity.dynamics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserValidatorEntity {

  private int codRet;
  private String userId;
  private String interId;
  private String message;
}
