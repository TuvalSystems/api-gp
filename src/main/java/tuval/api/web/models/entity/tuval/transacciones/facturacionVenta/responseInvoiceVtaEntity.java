package tuval.api.web.models.entity.tuval.transacciones.facturacionVenta;

public class responseInvoiceVtaEntity {
    
    public String documentoOrigen;
    public String idTipoDocumento;
    public String codigo;
    public String mensaje;
    public String tipoDocGenerado;
    public String numeroDocGenerado;
    public String getDocumentoOrigen() {
        return documentoOrigen;
    }
    public void setDocumentoOrigen(String documentoOrigen) {
        this.documentoOrigen = documentoOrigen;
    }
    public String getIdTipoDocumento() {
        return idTipoDocumento;
    }
    public void setIdTipoDocumento(String idTipoDocumento) {
        this.idTipoDocumento = idTipoDocumento;
    }
    public String getCodigo() {
        return codigo;
    }
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
    public String getMensaje() {
        return mensaje;
    }
    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
    public String getTipoDocGenerado() {
        return tipoDocGenerado;
    }
    public void setTipoDocGenerado(String tipoDocGenerado) {
        this.tipoDocGenerado = tipoDocGenerado;
    }
    public String getNumeroDocGenerado() {
        return numeroDocGenerado;
    }
    public void setNumeroDocGenerado(String numeroDocGenerado) {
        this.numeroDocGenerado = numeroDocGenerado;
    }
}
