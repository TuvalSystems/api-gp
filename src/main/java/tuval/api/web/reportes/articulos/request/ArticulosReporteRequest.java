package tuval.api.web.reportes.articulos.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ArticulosReporteRequest {
  @Size(max = 31) private String tx_itm;
  @Size(max = 15) private String codZafiro;
  @Size(max = 11) private String cod3L;
  @Size(max = 101) private String descripcion;
  @Size(max = 11) private String claseItem;
  @Size(max = 9) private String unidadMedida;
  @Size(max = 200) private String categoriaCompra;
  @Size(max = 200) private String lineaCompra;
  @Size(max = 200) private String claseCompra;
  @Size(max = 200) private String marca;
  @Size(max = 200) private String material;
  @Size(max = 31) private String itemCabecera;
  @Size(max = 3) private String catven;
  @Size(max = 3) private String catpes;
  @Size(max = 3) private String catfre;
  @Size(max = 3) private String cattuv;

  @NotNull @Min(0) private Integer page = 0;
  @NotNull @Min(1) @Max(500) private Integer size = 100;
  @NotNull private Sort sort = Sort.tx_itm;
  @NotNull private Direction direction = Direction.ASC;

  /** Closed allowlist: request text can never become an arbitrary SQL identifier. */
  public enum Sort {
    tx_itm, codZafiro, cod3L, Descripcion, unidadMedida, claseItem, precio, peso, costo,
    transito, cabGrupoVta, detGrupoVta, cabGrupoCom, detGrupoComp, promVtaMenAct,
    promVtaMenTeo, promVntaMenAnt, moq, fre, stokbase, exc, itemCabecera,
    categoriaCompra, lineaCompra, claseCompra, Marca, Material, compras, SubCompras,
    Control, SubControl, fechacreacion, catven, catpes, catfre, cattuv, marUti
  }

  public enum Direction { ASC, DESC }
}
