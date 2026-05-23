package tuval.api.web.models.request.bodegas.articulos;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TravapItemSelectionRequest {
    
    @NotNull(message = "La presión de trabajo es requerida")
    @DecimalMin(value = "0.0", inclusive = false, message = "La presión de trabajo debe ser mayor a 0")
    private BigDecimal workingPressurePsig; // Mapea a @nd_pretrab_psig

    @NotNull(message = "La presión de retorno es requerida")
    @DecimalMin(value = "0.0", message = "La presión de retorno no puede ser negativa")
    private BigDecimal returnPressurePsig; // Mapea a @nd_preret_psig

    @NotNull(message = "La carga estimada es requerida")
    @DecimalMin(value = "0.0", inclusive = false, message = "La carga debe ser mayor a 0")
    private BigDecimal estimatedLoadKg; // Mapea a @nd_carga_kg

    @NotNull(message = "El factor de seguridad es requerido")
    @DecimalMin(value = "1.0", message = "El factor de seguridad mínimo sugerido es 1.0")
    private BigDecimal safetyFactor;
}
