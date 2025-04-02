package tuval.api.web.models.entity.dynamics.usuario;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserValidatorEntity {

    @JsonProperty("USERID")
    private String userId;

    @JsonProperty("INTERID")
    private String interId;

    public UserValidatorEntity() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getInterId() {
        return interId;
    }

    public void setInterId(String interId) {
        this.interId = interId;
    }



    @Override
    public String toString() {
        return "{" +
                "userId='" + userId + '\'' +
                ", interId='" + interId + '\'' +
                '}';
    }

}
