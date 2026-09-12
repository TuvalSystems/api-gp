package tuval.api.web.cobranzas.cobros.dao.dynamics;

import java.sql.Types;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import tuval.api.web.common.util.CoerceHelperUtils;
import tuval.api.web.cobranzas.cobros.dto.CustomerCollectionDto;
import tuval.api.web.cobranzas.cobros.dto.CustomerInvoiceCheckReceiptDto;
import tuval.api.web.cobranzas.cobros.dto.CustomerPendingApplicationDocumentDto;

@Repository
public class CollectionDao {
  private final SimpleJdbcCall spCxC32;
  private final SimpleJdbcCall spCxC33;
  private final SimpleJdbcCall spCxC34;

  public CollectionDao(@Qualifier("dynamicsJdbcTemplate") JdbcTemplate jdbcTemplate) {
    JdbcTemplate dynamicsJdbcTemplate =
        Objects.requireNonNull(jdbcTemplate, "El bean dynamicsJdbcTemplate no puede ser null");

    this.spCxC32 =
        new SimpleJdbcCall(dynamicsJdbcTemplate)
            .withProcedureName("spCxC32")
            .withoutProcedureColumnMetaDataAccess()
            .declareParameters(
                new SqlParameter("SOPNUMBE", Types.VARCHAR), new SqlParameter("RUC", Types.VARCHAR))
            .returningResultSet("data", customerCollectionMapper());

    this.spCxC33 =
        new SimpleJdbcCall(dynamicsJdbcTemplate)
            .withProcedureName("spCxC33")
            .withoutProcedureColumnMetaDataAccess()
            .declareParameters(
                new SqlParameter("SOPNUMBE", Types.VARCHAR), new SqlParameter("RUC", Types.VARCHAR))
            .returningResultSet("data", customerInvoiceCheckReceiptMapper());

    this.spCxC34 =
        new SimpleJdbcCall(dynamicsJdbcTemplate)
            .withProcedureName("spCxC34")
            .withoutProcedureColumnMetaDataAccess()
            .declareParameters(
                new SqlParameter("DOCNUMBR", Types.VARCHAR), new SqlParameter("RUC", Types.VARCHAR))
            .returningResultSet("data", customerPendingApplicationDocumentDto());
  }

  @SuppressWarnings("unchecked")
  public List<CustomerCollectionDto> executeSpCxC32(String documento, String ruc) {

    MapSqlParameterSource params =
        new MapSqlParameterSource().addValue("SOPNUMBE", documento).addValue("RUC", ruc);
    Map<String, Object> result = spCxC32.execute(params);

    Object data = result.get("data");

    if (data == null) {
      return Collections.emptyList();
    }

    List<CustomerCollectionDto> rows = (List<CustomerCollectionDto>) data;

    return rows.stream().sorted(customerCollectionComparator()).toList();
  }

  @SuppressWarnings("unchecked")
  public List<CustomerInvoiceCheckReceiptDto> executeSpCxC33(String documento, String ruc) {

    MapSqlParameterSource params =
        new MapSqlParameterSource().addValue("SOPNUMBE", documento).addValue("RUC", ruc);
    Map<String, Object> result = spCxC33.execute(params);

    Object data = result.get("data");

    if (data == null) {
      return Collections.emptyList();
    }

    List<CustomerInvoiceCheckReceiptDto> rows = (List<CustomerInvoiceCheckReceiptDto>) data;

    return rows.stream().sorted(customerInvoiceCheckReceipComparator()).toList();
  }

  @SuppressWarnings("unchecked")
  public CustomerPendingApplicationDocumentDto executeSpCxC34(String documento, String ruc) {
    MapSqlParameterSource params =
        new MapSqlParameterSource().addValue("DOCNUMBR", documento).addValue("RUC", ruc);
    Map<String, Object> result = spCxC34.execute(params);

    List<CustomerPendingApplicationDocumentDto> rows =
        (List<CustomerPendingApplicationDocumentDto>) result.get("data");

    if (rows == null || rows.isEmpty()) {
      return null;
    }

    return rows.get(0);
  }

  private RowMapper<CustomerCollectionDto> customerCollectionMapper() {
    return (rs, rowNum) ->
        new CustomerCollectionDto(
            CoerceHelperUtils.emptyIfNull(rs.getString("DOCID")),
            CoerceHelperUtils.toLocalDate(rs.getDate("docdate")),
            CoerceHelperUtils.toLocalDate(rs.getDate("FechaAplicacionCobro")),
            CoerceHelperUtils.stringOrEmpty(rs.getString("mesCobro")),
            CoerceHelperUtils.stringOrEmpty(rs.getString("CUSTNMBR")),
            CoerceHelperUtils.stringOrEmpty(rs.getString("Cliente")),
            CoerceHelperUtils.stringOrEmpty(rs.getString("Establecimiento_Factura")),
            CoerceHelperUtils.stringOrEmpty(rs.getString("Factura")),
            CoerceHelperUtils.toBigDecimal(rs.getBigDecimal("Valor_Factura")),
            CoerceHelperUtils.toInt(rs.getInt("RMDTYPAL"), 0),
            CoerceHelperUtils.stringOrEmpty(rs.getString("Cobro")),
            CoerceHelperUtils.toBigDecimal(rs.getBigDecimal("Valor_Cobro")),
            CoerceHelperUtils.toLocalDate(rs.getDate("fechaDelDocumentoCobro")),
            CoerceHelperUtils.stringOrEmpty(rs.getString("Customer_PO_Number")),
            CoerceHelperUtils.stringOrEmpty(rs.getString("cf_numrecibo")));
  }

  private RowMapper<CustomerInvoiceCheckReceiptDto> customerInvoiceCheckReceiptMapper() {
    return (rs, rowNum) ->
        new CustomerInvoiceCheckReceiptDto(
            CoerceHelperUtils.stringOrEmpty(rs.getString("cq_numrecibo")),
            CoerceHelperUtils.stringOrEmpty(rs.getString("cq_idcliente")),
            CoerceHelperUtils.stringOrEmpty(rs.getString("cf_cobrogp")),
            CoerceHelperUtils.stringOrEmpty(rs.getString("cf_numfac")),
            CoerceHelperUtils.stringOrEmpty(rs.getString("cq_numchq")),
            CoerceHelperUtils.toBigDecimal(rs.getBigDecimal("montoCheque")),
            CoerceHelperUtils.toBigDecimal(rs.getBigDecimal("cf_montoapl")),
            CoerceHelperUtils.stringOrEmpty(rs.getString("cq_estado")),
            CoerceHelperUtils.toLocalDate(rs.getDate("cf_fecmod")));
  }

  private RowMapper<CustomerPendingApplicationDocumentDto> customerPendingApplicationDocumentDto() {

    return (rs, rowNum) ->
        new CustomerPendingApplicationDocumentDto(
            CoerceHelperUtils.stringOrEmpty(rs.getString("Identificacion")),
            CoerceHelperUtils.stringOrEmpty(rs.getString("Documento")),
            CoerceHelperUtils.stringOrEmpty(rs.getString("tipo")),
            CoerceHelperUtils.stringOrEmpty(rs.getString("Observacion")),
            CoerceHelperUtils.toLocalDate(rs.getDate("Fecha_Emision")),
            CoerceHelperUtils.stringOrEmpty(rs.getString("Condicion_Pago")),
            CoerceHelperUtils.toBigDecimal(rs.getBigDecimal("Monto_Original")),
            CoerceHelperUtils.toBigDecimal(rs.getBigDecimal("Saldo_Cliente")));
  }

  private Comparator<CustomerCollectionDto> customerCollectionComparator() {
    return Comparator.comparing(
            CustomerCollectionDto::getFactura,
            Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(
            CustomerCollectionDto::getFechaAplicacion,
            Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(
            CustomerCollectionDto::getDocumentoCobro,
            Comparator.nullsLast(Comparator.naturalOrder()));
  }

  private Comparator<CustomerInvoiceCheckReceiptDto> customerInvoiceCheckReceipComparator() {
    return Comparator.comparing(
        CustomerInvoiceCheckReceiptDto::getFactura,
        Comparator.nullsLast(Comparator.naturalOrder()));
  }
}
