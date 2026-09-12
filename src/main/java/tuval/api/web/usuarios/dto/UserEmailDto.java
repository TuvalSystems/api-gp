package tuval.api.web.usuarios.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEmailDto {
  private Integer coUsr;
  private String userName;
  private String fullName;
  private String email;
}
