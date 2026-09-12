package tuval.api.web.bodega.despachos.request.crearguia;

import java.math.BigDecimal;

public class DetailDeliveryNoteRequest {
    private String guiaRemision;
    private Integer secuencialDestinatario;
    private Integer secuenciaDetalle;
    private String identificacionDestinatario;
    private String docSustento;
    private String codInterno;
    private String codAdicional;
    private String descripcion;
    private BigDecimal  cantidad;
    private String unidadMedida;
    private String guiaBodega;
    public String getGuiaRemision() {
        return guiaRemision;
    }
    public void setGuiaRemision(String guiaRemision) {
        this.guiaRemision = guiaRemision;
    }
    public Integer getSecuencialDestinatario() {
        return secuencialDestinatario;
    }
    public void setSecuencialDestinatario(Integer secuencialDestinatario) {
        this.secuencialDestinatario = secuencialDestinatario;
    }
    public Integer getSecuenciaDetalle() {
        return secuenciaDetalle;
    }
    public void setSecuenciaDetalle(Integer secuenciaDetalle) {
        this.secuenciaDetalle = secuenciaDetalle;
    }
    public String getIdentificacionDestinatario() {
        return identificacionDestinatario;
    }
    public void setIdentificacionDestinatario(String identificacionDestinatario) {
        this.identificacionDestinatario = identificacionDestinatario;
    }
    public String getDocSustento() {
        return docSustento;
    }
    public void setDocSustento(String docSustento) {
        this.docSustento = docSustento;
    }
    public String getCodInterno() {
        return codInterno;
    }
    public void setCodInterno(String codInterno) {
        this.codInterno = codInterno;
    }
    public String getCodAdicional() {
        return codAdicional;
    }
    public void setCodAdicional(String codAdicional) {
        this.codAdicional = codAdicional;
    }
    public String getDescripcion() {
        return descripcion;
    }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    public BigDecimal getCantidad() {
        return cantidad;
    }
    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }
    public String getUnidadMedida() {
        return unidadMedida;
    }
    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }
    public String getGuiaBodega() {
        return guiaBodega;
    }
    public void setGuiaBodega(String guiaBodega) {
        this.guiaBodega = guiaBodega;
    }
   


}
