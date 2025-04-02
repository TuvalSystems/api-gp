package tuval.api.web.models.entity.tuval.sucursales;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CoordinateListEntity {
    
    @JsonProperty("id_Cliente")
    private String idCliente;

    @JsonProperty("AddressCode")
    private String addressCode;

    @JsonProperty("DatosCoordenadas")
    private String datosCoordenadas;
    public String getIdCliente() {
        return idCliente;
    }
    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }
    public String getAddressCode() {
        return addressCode;
    }
    public void setAddressCode(String addressCode) {
        this.addressCode = addressCode;
    }
    public String getDatosCoordenadas() {
        return datosCoordenadas;
    }
    public void setDatosCoordenadas(String datosCoordenadas) {
        this.datosCoordenadas = datosCoordenadas;
    } 

   
}
