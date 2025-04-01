package tuval.api.web.models.entity.sucursales;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BranchesClientEntity {
    
    @JsonProperty("CLIENTE")
    private String cliente ;

    @JsonProperty("DESCRIPCION_CLIENTE")
    private String descripcion ;

    @JsonProperty("DIRECCIONES")    
    private List<BranchesClientAddressEntity> direcciones ;


    public BranchesClientEntity() {
    }
    public String getCliente() {
        return cliente;
    }
    public void setCliente(String cliente) {
        this.cliente = cliente;
    }
    public String getDescripcion() {
        return descripcion;
    }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    public List<BranchesClientAddressEntity> getDirecciones() {
        return direcciones;
    }
    public void setDirecciones(List<BranchesClientAddressEntity> direcciones) {
        this.direcciones = direcciones;
    }

    @Override
    public String toString() {
        return "BranchesClientEntity [cliente=" + cliente + ", descripcion=" + descripcion + ", direcciones=" + direcciones
                + "]";
    }

}
