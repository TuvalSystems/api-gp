package tuval.api.web.ventas.cotizaciones.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuoteHeaderRequest {
    private String cotizacion;
    private String estado;
}
