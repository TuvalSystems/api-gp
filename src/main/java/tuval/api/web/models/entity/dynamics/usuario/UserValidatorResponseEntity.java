package tuval.api.web.models.entity.dynamics.usuario;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserValidatorResponseEntity {
    
     @JsonProperty("codret")
    private int codret;
    @JsonProperty("msj")
    private String msj;
    // private String userId;
    // private String interId;
    private UserValidatorEntity data;
    
    public UserValidatorResponseEntity() {
    }

    public int getCodRet() {
        return codret;
    }

    public void setCodRet(int codret) {
        this.codret = codret;
    }

    public String getMsj() {
        return msj;
    }

    public void setMsj(String msj) {
        this.msj = msj;
    }

    // public String getUserId() {
    //     return userId;
    // }

    // public void setUserId(String userId) {
    //     this.userId = userId;
    // }

    // public String getInterId() {
    //     return interId;
    // }

    // public void setInterId(String interId) {
    //     this.interId = interId;
    // }

    public UserValidatorEntity getData() {
        return data;
    }

    public void setData(UserValidatorEntity data) {
        this.data = data;
    }
}
