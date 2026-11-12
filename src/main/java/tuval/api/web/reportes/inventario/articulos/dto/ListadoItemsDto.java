package tuval.api.web.reportes.inventario.articulos.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record ListadoItemsDto(
    String txItm,
    String codZafiro,
    String cod3L,
    String descripcion,
    String unidadMedida,
    String txAtrMar,
    String txAtrMat) {}
