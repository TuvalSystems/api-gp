package tuval.api.web.models.request.bodegas.articulos;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemsExpoRequest {
    @NotBlank(message = "El código del ítem es obligatorio")
    private String item;
    @NotBlank(message = "El punto de venta es obligatorio")
    private String puntoVenta;
}
