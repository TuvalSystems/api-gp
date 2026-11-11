package tuval.api.web.reportes.inventario.varianza.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record VarianzaDto(
    Integer IVDOCTYP,
    String IVDOCNBR,
    @JsonFormat(shape = JsonFormat.Shape.STRING) LocalDate DOCDATE,
    String BACHNUMB,
    String ITEMNMBR,
    String ITMGEDSC,
    String ITMSHNAM,
    String ITEMDESC,
    BigDecimal LNSEQNBR,
    BigDecimal QTYBSUOM,
    String TRXLOCTN,
    BigDecimal TRXQTY,
    BigDecimal UNITCOST,
    BigDecimal EXTDCOST,
    String Reason_Code,
    BigDecimal CURRCOST,
    BigDecimal EXISTENCIA) {}
