package tuval.api.web.reportes.resolucionarticulos.dto;

// Deliberately independent of the report DTO containing financial information.
public record CatalogoArticuloDto(String codigoGP, String codigoZafiro, String codigo3L,
    String descripcion, String unidad, String marca, String material,
    String categoriaCompra, String lineaCompra, String claseCompra) {}
