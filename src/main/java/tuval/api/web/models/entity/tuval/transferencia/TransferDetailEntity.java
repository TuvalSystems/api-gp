package tuval.api.web.models.entity.tuval.transferencia;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TransferDetailEntity {

    @JsonProperty("documento")
    private String Documento;
    @JsonProperty("origen")
    private String Origen;
    @JsonProperty("destino")
    private String Destino;
    @JsonProperty("item")
    private String Item;
    @JsonProperty("nombreItem")
    private String NombreItem;
    @JsonProperty("cantidad")
    private float Cantidad;
    @JsonProperty("trd_ubic")
    private String trd_ubic;

    public String getTrd_ubic() {
        return trd_ubic;
    }


    public void setTrd_ubic(String trd_ubic) {
        this.trd_ubic = trd_ubic;
    }

    public TransferDetailEntity() {
    }

    public String getDocumento() {
        return Documento;
    }

    public void setDocumento(String Documento) {
        this.Documento = Documento;
    }

    public String getOrigen() {
        return Origen;
    }

    public void setOrigen(String Origen) {
        this.Origen = Origen;
    }

    public String getDestino() {
        return Destino;
    }

    public void setDestino(String Destino) {
        this.Destino = Destino;
    }

    public String getItem() {
        return Item;
    }

    public void setItem(String Item) {
        this.Item = Item;
    }

    public String getNombreItem() {
        return NombreItem;
    }

    public void setNombreItem(String NombreItem) {
        this.NombreItem = NombreItem;
    }

    public float getCantidad() {
        return Cantidad;
    }

    public void setCantidad(float Cantidad) {
        this.Cantidad = Cantidad;
    }

    @Override
    public String toString() {
        return "TransferDetailEntity{" + "Documento=" + Documento + ", Origen=" + Origen + ", Destino=" + Destino
                + ", Item=" + Item + ", NombreItem=" + NombreItem + ", Cantidad=" + Cantidad +  ", trd_ubic=" + trd_ubic + '}';
    }

}
