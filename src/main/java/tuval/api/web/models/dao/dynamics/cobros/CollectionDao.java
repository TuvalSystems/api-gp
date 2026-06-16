package tuval.api.web.models.dao.dynamics.cobros;

import java.sql.Date;
import java.sql.Types;
import java.time.LocalDate;
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
import tuval.api.web.models.dto.cobros.CustomerCollectionDto;
import tuval.api.web.models.dto.cobros.CustomerInvoiceCheckReceiptDto;

@Repository
public class CollectionDao {
  private final SimpleJdbcCall spCxC32;
  private final SimpleJdbcCall spCxC33;

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

  private RowMapper<CustomerCollectionDto> customerCollectionMapper() {
    return (rs, rowNum) ->
        new CustomerCollectionDto(
            rs.getString("DOCID"),
            toLocalDate(rs.getDate("docdate")),
            toLocalDate(rs.getDate("FechaAplicacionCobro")),
            rs.getString("mesCobro"),
            rs.getString("CUSTNMBR"),
            rs.getString("Cliente"),
            rs.getString("Establecimiento_Factura"),
            rs.getString("Factura"),
            rs.getBigDecimal("Valor_Factura"),
            rs.getInt("RMDTYPAL"),
            rs.getString("Cobro"),
            rs.getBigDecimal("Valor_Cobro"),
            rs.getDate("fechaDelDocumentoCobro") != null
                ? rs.getDate("fechaDelDocumentoCobro").toLocalDate()
                : null,
            rs.getString("Customer_PO_Number"),
            rs.getString("cf_numrecibo"));
  }

  private RowMapper<CustomerInvoiceCheckReceiptDto> customerInvoiceCheckReceiptMapper() {
    return (rs, rowNum) ->
        new CustomerInvoiceCheckReceiptDto(
            rs.getString("cq_numrecibo"),
            rs.getString("cq_idcliente"),
            rs.getString("cf_cobrogp").trim(),
            rs.getString("cf_numfac"),
            rs.getString("cq_numchq"),
            rs.getBigDecimal("montoCheque"),
            rs.getBigDecimal("cf_montoapl"),
            rs.getString("cq_estado"),
            toLocalDate(rs.getDate("cf_fecmod")));
  }

  private LocalDate toLocalDate(Date date) {
    return date != null ? date.toLocalDate() : null;
  }

  private Comparator<CustomerCollectionDto> customerCollectionComparator() {
    return Comparator.comparing(
            CustomerCollectionDto::getInvoiceNumber,
            Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(
            CustomerCollectionDto::getCollectionApplyDate,
            Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(
            CustomerCollectionDto::getCollectionId,
            Comparator.nullsLast(Comparator.naturalOrder()));
  }

  private Comparator<CustomerInvoiceCheckReceiptDto> customerInvoiceCheckReceipComparator() {
    return Comparator.comparing(
        CustomerInvoiceCheckReceiptDto::getInvoice,
        Comparator.nullsLast(Comparator.naturalOrder()));
  }
}
