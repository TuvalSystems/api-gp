package tuval.api.web.reportes.listadoitems.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record ListadoItemsReporteDto(
    String tx_itm,
    String codZafiro,
    String cod3L,
    @JsonProperty("Descripcion") String descripcion,
    String unidadMedida,
    String tx_atrMar,
    String tx_atrMat) {}
