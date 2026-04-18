package tuval.api.web.models.dto.admin;

import java.math.BigDecimal;
import java.time.LocalDate;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncClientsDto {
    
    private String ruc;
    private String razonSocial;
    private String nombreCorto;
    private String idClase;
    private String prioridad;
    private String dirPrincipal;
    private String contacto;
    private String direccion;
    private String ciudad;
    private String provincia;
    private String pais;
    private String territorio;
    private String estado;
    private String suspendido;
    private String telefono1;
    private String telefono2;
    private String telefono3;
    private String telefono4;
    private LocalDate fechaModif;
    private LocalDate fechaCreacion;
    private String emailSri;
    private String emailInterno;
    private BigDecimal impVta;
    private String dirEnvio;
    private String dirFactura;
    private String vendedor;
    private BigDecimal descComercial;
    private String condPago;
    private Integer diaGraciaCredito;
    private Integer diaGraciaCheque;
    private String nivelPrecio;
    private String tipoCredito;
    private BigDecimal montoCredito;
    private String grupoEconomico;
    private String grupo;
    private String subGrupo;
    private Integer diaMaxMesFacturacion;
    private Integer factPostAlDespacho;
    private String retFte;
    private String retIva;

}
