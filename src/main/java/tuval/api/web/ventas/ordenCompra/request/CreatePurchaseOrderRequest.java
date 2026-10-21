package tuval.api.web.ventas.ordenCompra.request;

import java.math.BigDecimal;
import jakarta.validation.constraints.*;

public record CreatePurchaseOrderRequest(
    @NotBlank String proveedor,
    @NotBlank String usuario,
    @NotNull Integer tipoDespachoId,
    @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal valorOrdenCompra) {}

