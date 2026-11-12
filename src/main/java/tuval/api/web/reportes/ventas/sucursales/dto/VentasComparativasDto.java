package tuval.api.web.reportes.ventas.sucursales.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record VentasComparativasDto(
    String txNom,
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
