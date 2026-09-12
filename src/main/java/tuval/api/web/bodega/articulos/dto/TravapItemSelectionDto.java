package tuval.api.web.bodega.articulos.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TravapItemSelectionDto {

    private String stSel;
    private String txItem;
    private String txVentec;
    private Integer coTravapmod;
    private String txTiptra;
    private Integer nePmoPsig;
    private String txPipe;
    private BigDecimal ndPipe;
    private BigDecimal ndPretrabPsig;
    private BigDecimal ndPreretPsig;
    private BigDecimal ndPredifCalcPsig;
    private BigDecimal ndPredifTabPsig;
    private BigDecimal ndCargaKg;
    private BigDecimal ndFacseg;
    private BigDecimal ndCargaCalcKg;
    private BigDecimal ndCargaLbh;
    private BigDecimal ndCaplbh;
    private BigDecimal ndFacCob;
    private String txMensaje;


}
