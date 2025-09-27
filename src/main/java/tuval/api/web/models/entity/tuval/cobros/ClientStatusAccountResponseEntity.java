package tuval.api.web.models.entity.tuval.cobros;

import java.util.List;

public class ClientStatusAccountResponseEntity {

    public ClientStatusAccountHeaderEntity cabecera;
    public List<ClientStatusAccountDetailEntity> detalle;
    public ClientStatusAccountHeaderEntity getCabecera() {
        return cabecera;
    }
    public void setCabecera(ClientStatusAccountHeaderEntity cabecera) {
        this.cabecera = cabecera;
    }
    public List<ClientStatusAccountDetailEntity> getDetalle() {
        return detalle;
    }
    public void setDetalle(List<ClientStatusAccountDetailEntity> detalle) {
        this.detalle = detalle;
    }
}
