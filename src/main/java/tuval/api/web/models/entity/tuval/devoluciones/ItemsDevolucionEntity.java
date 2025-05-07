package tuval.api.web.models.entity.tuval.devoluciones;

public class ItemsDevolucionEntity {
    
    private String item;
    private float cantidad;
    private String compartimento;
    public String getItem() {
        return item;
    }
    public void setItem(String item) {
        this.item = item;
    }
    public float getCantidad() {
        return cantidad;
    }
    public void setCantidad(float cantidad) {
        this.cantidad = cantidad;
    }
    public String getCompartimento() {
        return compartimento;
    }
    public void setCompartimento(String compartimento) {
        this.compartimento = compartimento;
    }
}
