package tuval.api.web.reportes.articulos.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record ArticulosReporteDto(
    String tx_itm,
    String codZafiro,
    String cod3L,
    @JsonProperty("Descripcion") String descripcion,
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
    @JsonProperty("Marca") String marca,
    @JsonProperty("Material") String material,
    String compras,
    @JsonProperty("SubCompras") String subCompras,
    @JsonProperty("Control") String control,
    @JsonProperty("SubControl") String subControl,
    @JsonFormat(shape = JsonFormat.Shape.STRING) LocalDateTime fechacreacion,
    String catven,
    String catpes,
    String catfre,
    String cattuv,
    BigDecimal marUti) {}
