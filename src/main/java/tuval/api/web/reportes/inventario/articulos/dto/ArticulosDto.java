package tuval.api.web.reportes.inventario.articulos.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record ArticulosDto(
    String txItm,
    String codZafiro,
    String cod3L,
    String descripcion,
    String unidadMedida,
    String claseItem,
    BigDecimal precio,
    BigDecimal peso,
    BigDecimal costo,
    BigDecimal transito,
    Short cabGrupoVta,
    Short detGrupoVta,
    Short cabGrupoCom,
    Short detGrupoComp,
    BigDecimal promVtaMenAct,
    BigDecimal promVtaMenTeo,
    BigDecimal promVntaMenAnt,
    BigDecimal moq,
    BigDecimal fre,
    BigDecimal stokbase,
    BigDecimal exc,
    String itemCabecera,
    String categoriaCompra,
    String lineaCompra,
    String claseCompra,
    String marca,
    String material,
    String compras,
    String subCompras,
    String control,
    String subControl,
    @JsonFormat(shape = JsonFormat.Shape.STRING) LocalDateTime fechacreacion,
    String catven,
    String catpes,
    String catfre,
    String cattuv,
    BigDecimal marUti) {}
