package tuval.api.web.reportes.ven11.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record Ven11ReporteDto(
    String tx_nom,
    BigDecimal subTotalDiaHoy,
    BigDecimal extCostDiaHoy,
    BigDecimal subTotalAyer,
    BigDecimal extCostAyer,
    BigDecimal subTotalMes,
    BigDecimal extCostMes,
    BigDecimal subtotalMesAnterior,
    BigDecimal extCostMesAnterior,
    BigDecimal subtotalMesAnterior2,
    BigDecimal extCostMesAnterior2,
    BigDecimal subtotalAnioActual,
    BigDecimal extCostAnioActual,
    BigDecimal subtotalAnioAtras,
    BigDecimal extCostAnioAtras) {}
