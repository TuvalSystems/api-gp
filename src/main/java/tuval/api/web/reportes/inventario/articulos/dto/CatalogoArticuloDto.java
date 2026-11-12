package tuval.api.web.reportes.inventario.articulos.dto;

// Raw descriptive fields from vw_listadoItems. A unit schedule is not a sales unit,
// and source attributes are not assumed equivalent to the old master's labels.
public record CatalogoArticuloDto(String codigoGP, String codigoZafiro, String codigo3L,
    String descripcion, String esquemaUnidad, String atributoMarca, String atributoMaterial) {}
