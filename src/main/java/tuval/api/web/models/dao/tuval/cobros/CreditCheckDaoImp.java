package tuval.api.web.models.dao.tuval.cobros;

import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import tuval.api.web.models.Utils.CoerceHelperUtils;
import tuval.api.web.models.dto.cobros.BusinessDaysDto;
import tuval.api.web.models.dto.cobros.CheckValidatorDto;
import tuval.api.web.models.dto.cobros.ClientOverdueDocumentDto;
import tuval.api.web.models.dto.cobros.CustomerPendingApplicationDocumentDto;
import tuval.api.web.models.dto.cobros.CustomerResumenAccountDto;
import tuval.api.web.models.dto.cobros.CustomerStatusAccountDto;
import tuval.api.web.models.entity.tuval.cobros.ClientSinSoporteEntity;
import tuval.api.web.models.entity.tuval.cobros.ClientStatusAccountDetailEntity;
import tuval.api.web.models.entity.tuval.cobros.ClientStatusAccountHeaderEntity;
import tuval.api.web.models.entity.tuval.cobros.ListClientOverdueDocumentEntity;
import tuval.api.web.models.entity.tuval.cobros.ListaClientSinSoporteEntity;

@Repository
public class CreditCheckDaoImp implements CreditCheckDao {

  private final JdbcTemplate jdbcTemplate;
  private final SimpleJdbcCall spValidacionCrediticia;
  private final SimpleJdbcCall spConsultaDocSaldo;

  public CreditCheckDaoImp(@Qualifier("tuvalJdbcTemplate") JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
    this.spValidacionCrediticia =
        new SimpleJdbcCall(jdbcTemplate)
            .withProcedureName("sp_mc_validacion_creditica")
            .withoutProcedureColumnMetaDataAccess()
            .declareParameters(
                new SqlParameter("modo", Types.INTEGER),
                new SqlParameter("idcliente", Types.VARCHAR),
                new SqlParameter("idvendedor", Types.VARCHAR),
                new SqlParameter("documento", Types.VARCHAR),
                new SqlParameter("fecini", Types.DATE),
                new SqlParameter("fecfin", Types.DATE),
                new SqlParameter("userid", Types.VARCHAR),
                new SqlParameter("equipo", Types.VARCHAR),
                new SqlParameter("codret", Types.INTEGER),
                new SqlParameter("msj", Types.VARCHAR));

    this.spConsultaDocSaldo =
        new SimpleJdbcCall(jdbcTemplate)
            .withProcedureName("sp_mc_consulta_docsaldo")
            .withoutProcedureColumnMetaDataAccess()
            .declareParameters(
                new SqlParameter("modo", Types.INTEGER),
                new SqlParameter("fecpro", Types.TIMESTAMP),
                new SqlParameter("idcliente", Types.VARCHAR),
                new SqlParameter("idvendedor", Types.VARCHAR),
                new SqlParameter("documento", Types.VARCHAR),
                new SqlParameter("numcobro", Types.VARCHAR),
                new SqlParameter("factura", Types.VARCHAR),
                new SqlParameter("fecini", Types.TIMESTAMP),
                new SqlParameter("saldofac", Types.DECIMAL),
                new SqlParameter("userid", Types.VARCHAR),
                new SqlParameter("equipo", Types.VARCHAR),
                new SqlOutParameter("codret", Types.INTEGER),
                new SqlOutParameter("msj", Types.VARCHAR));
  }

  @Override
  public ClientOverdueDocumentDto executeSpConsultExpiredClientDocument(
      String ruc, String idCompania, int modo) {

    MapSqlParameterSource in =
        new MapSqlParameterSource()
            .addValue("idcliente", ruc)
            .addValue("userid", "wsrvmc")
            .addValue("modo", modo)
            .addValue("equipo", "wserver")
            .addValue("idvendedor", "")
            .addValue("documento", "")
            .addValue("fecini", "")
            .addValue("fecfin", "")
            .addValue("codret", "")
            .addValue("msj", "");
    try {
      Map<String, Object> out = spValidacionCrediticia.execute(in);
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> rs = (List<Map<String, Object>>) out.get("#result-set-1");
      if (rs == null || rs.isEmpty()) {
        throw new RuntimeException("SP no retornó filas para cliente=" + ruc);
      }
      Map<String, Object> row = rs.get(0);
      ClientOverdueDocumentDto e = new ClientOverdueDocumentDto();
      e.setRuc(CoerceHelperUtils.stringOrEmpty(row.get("RUC")));
      e.setFactura(CoerceHelperUtils.stringOrEmpty(row.get("FACTURA")));
      e.setfEmision(CoerceHelperUtils.toLocalDate(row.get("F_EMISION")));
      e.setfVencimiento(CoerceHelperUtils.toLocalDate(row.get("F_VENCIMIENTO")));
      e.setDiasVencidos(CoerceHelperUtils.toIntObject(row.get("DIAS_VENCIDOS")));
      e.setPendienteGpMc(CoerceHelperUtils.toBigDecimal(row.get("PENDIENTE_GP_MC")));

      return e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public ListClientOverdueDocumentEntity executeSpConsultExpiredClientDocumentList(
      String ruc, String idCompania, int modo) {

    MapSqlParameterSource in =
        new MapSqlParameterSource()
            .addValue("idcliente", ruc)
            .addValue("userid", "wsrvmc")
            .addValue("modo", modo)
            .addValue("equipo", "wserver")
            .addValue("idvendedor", "")
            .addValue("documento", "")
            .addValue("fecini", "")
            .addValue("fecfin", "")
            .addValue("codret", "")
            .addValue("msj", "");
    try {
      Map<String, Object> out = spValidacionCrediticia.execute(in);
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> rs = (List<Map<String, Object>>) out.get("#result-set-1");
      if (rs == null || rs.isEmpty()) {
        throw new RuntimeException("SP no retornó filas para cliente=" + ruc);
      }
      List<ClientOverdueDocumentDto> lista = new ArrayList<>();
      for (Map<String, Object> row : rs) {
        ClientOverdueDocumentDto e = new ClientOverdueDocumentDto();
        e.setRuc(CoerceHelperUtils.stringOrEmpty(row.get("RUC")));
        e.setFactura(CoerceHelperUtils.stringOrEmpty(row.get("FACTURA")));
        e.setfEmision(CoerceHelperUtils.toLocalDate(row.get("F_EMISION")));
        e.setfVencimiento(CoerceHelperUtils.toLocalDate(row.get("F_VENCIMIENTO")));
        e.setDiasVencidos(CoerceHelperUtils.toIntObject(row.get("DIAS_VENCIDOS")));
        e.setPendienteGpMc(CoerceHelperUtils.toBigDecimal(row.get("PENDIENTE_GP_MC")));

        lista.add(e);
      }
      ListClientOverdueDocumentEntity result = new ListClientOverdueDocumentEntity();
      result.setData(lista);
      return result;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public CheckValidatorDto getConsultCreditValidation(String ruc, String idCompania, int modo) {
    MapSqlParameterSource in =
        new MapSqlParameterSource()
            .addValue("idcliente", ruc)
            .addValue("userid", "wsrvmc")
            .addValue("modo", modo)
            .addValue("equipo", "wserver")
            .addValue("idvendedor", "")
            .addValue("documento", "")
            .addValue("fecini", "")
            .addValue("fecfin", "")
            .addValue("codret", "")
            .addValue("msj", "");
    try {
      Map<String, Object> out = spValidacionCrediticia.execute(in);
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> rs = (List<Map<String, Object>>) out.get("#result-set-1");
      if (rs == null || rs.isEmpty()) {
        throw new RuntimeException("SP no retornó filas para cliente=" + ruc);
      }

      Map<String, Object> row = rs.get(0);
      CheckValidatorDto e = new CheckValidatorDto();
      e.setRuc(CoerceHelperUtils.stringOrEmpty(row.get("RUC")));
      e.setCupoAsignado(CoerceHelperUtils.toDouble(row.get("CUPO")));
      e.setCupoDisponibleGp(CoerceHelperUtils.toDouble(row.get("CUPO_PERMITIDO")));
      e.setSuspendido(CoerceHelperUtils.toIntObject(row.get("SUSPENDIDO")));
      return e;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(
          "Fallo en obtener el cupo del cliente, cliente="
              + ruc
              + ", modo="
              + modo
              + ": "
              + e.getMessage(),
          e);
    }
  }

  @Override
  public ClientSinSoporteEntity executeSpConsultDocumentClientDontSupport(
      String ruc, String idCompania, int modo) {

    MapSqlParameterSource in =
        new MapSqlParameterSource()
            .addValue("idcliente", ruc)
            .addValue("userid", "wsrvmc")
            .addValue("modo", modo)
            .addValue("equipo", "wserver")
            .addValue("idvendedor", "")
            .addValue("documento", "")
            .addValue("fecini", "")
            .addValue("fecfin", "")
            .addValue("codret", "")
            .addValue("msj", "");
    try {
      Map<String, Object> out = spValidacionCrediticia.execute(in);
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> rs = (List<Map<String, Object>>) out.get("#result-set-1");
      if (rs == null || rs.isEmpty()) {
        throw new RuntimeException("SP no retornó filas para cliente=" + ruc);
      }

      Map<String, Object> row = rs.get(0);
      ClientSinSoporteEntity e = new ClientSinSoporteEntity();
      e.setRuc(CoerceHelperUtils.stringOrEmpty(row.get("RUC")));
      e.setFactura(CoerceHelperUtils.stringOrEmpty(row.get("FACTURA")));
      e.setfEmision(CoerceHelperUtils.toLocalDate(row.get("F_EMISION")));
      e.setfVencimiento(CoerceHelperUtils.toLocalDate(row.get("F_VENCIMIENTO")));
      e.setSaldoPendiente(CoerceHelperUtils.toBigDecimal(row.get("PENDIENTE_GP_MC")));
      return e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public ListaClientSinSoporteEntity executeSpConsultDocumentClientDontSupport2(
      String ruc, String idCompania, int modo) {

    MapSqlParameterSource in =
        new MapSqlParameterSource()
            .addValue("idcliente", ruc)
            .addValue("userid", "wsrvmc")
            .addValue("modo", modo)
            .addValue("equipo", "wserver")
            .addValue("idvendedor", "")
            .addValue("documento", "")
            .addValue("fecini", "")
            .addValue("fecfin", "")
            .addValue("codret", "")
            .addValue("msj", "");
    try {
      Map<String, Object> out = spValidacionCrediticia.execute(in);
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> rs = (List<Map<String, Object>>) out.get("#result-set-1");
      if (rs == null || rs.isEmpty()) {
        throw new RuntimeException("SP no retornó filas para cliente=" + ruc);
      }

      List<ClientSinSoporteEntity> lista = new ArrayList<>();

      // Recorrer todas las filas devueltas por el SP
      for (Map<String, Object> row : rs) {
        ClientSinSoporteEntity e = new ClientSinSoporteEntity();
        e.setRuc(CoerceHelperUtils.stringOrEmpty(row.get("RUC")));
        e.setFactura(CoerceHelperUtils.stringOrEmpty(row.get("FACTURA")));
        e.setfEmision(CoerceHelperUtils.toLocalDate(row.get("F_EMISION")));
        e.setfVencimiento(CoerceHelperUtils.toLocalDate(row.get("F_VENCIMIENTO")));
        e.setSaldoPendiente(CoerceHelperUtils.toBigDecimal(row.get("PENDIENTE_GP_MC")));

        lista.add(e); // 👈 asumir que tu entidad tiene un add() o setLista()
      }
      ListaClientSinSoporteEntity result = new ListaClientSinSoporteEntity();
      result.setData(lista);
      return result;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public ClientStatusAccountHeaderEntity executeSpConsultClientStatusAccountHeader(
      String ruc, int modo) {
    MapSqlParameterSource in =
        new MapSqlParameterSource()
            .addValue("modo", modo)
            .addValue("fecpro", "")
            .addValue("idcliente", ruc)
            .addValue("idvendedor", "")
            .addValue("documento", "")
            .addValue("factura", "")
            .addValue("numcobro", "")
            .addValue("fecini", "")
            .addValue("saldofac", null)
            .addValue("userid", "wsrvmc")
            .addValue("equipo", "wserver")
            .addValue("codret", null)
            .addValue("msj", "");
    try {
      Map<String, Object> out = spConsultaDocSaldo.execute(in);
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> rows = (List<Map<String, Object>>) out.get("#result-set-1");
      // 1) Validar que haya filas
      if (rows == null || rows.isEmpty()) {
        Integer codret = (Integer) out.get("codret");
        String msj = (String) out.get("msj");
        throw new RuntimeException(
            "SP no retornó filas para cliente="
                + ruc
                + " (codret="
                + codret
                + ", msj="
                + msj
                + ")");
      }

      ClientStatusAccountHeaderEntity e = new ClientStatusAccountHeaderEntity();
      Map<String, Object> row = rows.get(0);
      e.setIdentificacion(CoerceHelperUtils.stringOrEmpty(row.get("Identificacion")));
      e.setCupo(CoerceHelperUtils.toBigDecimal(String.valueOf(row.get("Cupo")))); // numeric(19,5)
      e.setSuspendido(CoerceHelperUtils.stringOrEmpty(row.get("Suspendido")));
      e.setMensaje(CoerceHelperUtils.stringOrEmpty(row.get("Mensaje")));
      return e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<ClientStatusAccountDetailEntity> executeSpConsultClientStatusAccountDetail(
      String ruc, int modo) {
    MapSqlParameterSource in =
        new MapSqlParameterSource()
            .addValue("modo", modo)
            .addValue("fecpro", null, Types.TIMESTAMP)
            .addValue("idcliente", ruc)
            .addValue("idvendedor", null, Types.VARCHAR)
            .addValue("documento", null, Types.VARCHAR)
            .addValue("numcobro", null, Types.VARCHAR)
            .addValue("factura", null, Types.VARCHAR)
            .addValue("fecini", null, Types.TIMESTAMP)
            .addValue("saldofac", null, Types.DECIMAL)
            .addValue("userid", "wsrvmc")
            .addValue("equipo", "wserver")
            .addValue("codret", null)
            .addValue("msj", "");
    try {
      Map<String, Object> out = spConsultaDocSaldo.execute(in);
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> rs = (List<Map<String, Object>>) out.get("#result-set-1");
      if (rs == null || rs.isEmpty()) {
        throw new RuntimeException("SP no retornó filas para cliente=" + ruc);
      }
      List<ClientStatusAccountDetailEntity> lista = new ArrayList<>();
      // Recorrer todas las filas devueltas por el SP
      for (Map<String, Object> row : rs) {
        ClientStatusAccountDetailEntity e = new ClientStatusAccountDetailEntity();
        e.setIdentificacion(CoerceHelperUtils.stringOrEmpty(row.get("Identificacion")));
        e.setDocumento(CoerceHelperUtils.stringOrEmpty(row.get("Documento")));
        e.setTipo(CoerceHelperUtils.stringOrEmpty(row.get("Tipo")));
        e.setVendedor(CoerceHelperUtils.stringOrEmpty(row.get("vendedor")));
        e.setFechaEmision(CoerceHelperUtils.toLocalDate(row.get("Fecha_Emision")));
        e.setFechaVencimiento(CoerceHelperUtils.toLocalDate(row.get("Fecha_Vencimiento")));
        e.setCondicionPago(CoerceHelperUtils.stringOrEmpty(row.get("Condicion_Pago")));
        e.setMontoOriginal(CoerceHelperUtils.toBigDecimal(row.get("Monto_Original")));
        e.setSaldoBaseCliente(CoerceHelperUtils.toBigDecimal(row.get("Saldo_Base_Cliente")));
        e.setSaldoCliente(CoerceHelperUtils.toBigDecimal(row.get("Saldo_Cliente")));
        lista.add(e); // 👈 asumir que tu entidad tiene un add() o setLista()
      }
      return lista;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public BusinessDaysDto executeSpGetBusinessDaysCheck(String fecha, int modo) {

    Timestamp fechaTimestamp = null;
    try {
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      Date parsedDate = dateFormat.parse(fecha);
      fechaTimestamp = new Timestamp(parsedDate.getTime());
      MapSqlParameterSource in =
          new MapSqlParameterSource()
              .addValue("modo", modo)
              .addValue("fecpro", fechaTimestamp, Types.TIMESTAMP)
              .addValue("idcliente", null, Types.VARCHAR)
              .addValue("idvendedor", null, Types.VARCHAR)
              .addValue("documento", null, Types.VARCHAR)
              .addValue("numcobro", null, Types.VARCHAR)
              .addValue("factura", null, Types.VARCHAR)
              .addValue("fecini", null, Types.TIMESTAMP)
              .addValue("saldofac", null, Types.DECIMAL)
              .addValue("userid", "wsrvmc")
              .addValue("equipo", "wserver")
              .addValue("codret", null)
              .addValue("msj", null);

      Map<String, Object> out = spConsultaDocSaldo.execute(in);
      BusinessDaysDto response = new BusinessDaysDto();
      response.setCodigo(CoerceHelperUtils.toIntObject(out.get("codret")));
      response.setDescripcion(CoerceHelperUtils.stringOrEmpty(out.get("msj")));
      return response;
    } catch (Exception e) {
      throw new RuntimeException("Error en executeSpGetBusinessDaysCheck: " + e.getMessage(), e);
    }
  }

  @Override
  public List<CustomerPendingApplicationDocumentDto> executeSpGetApplyPendingPayment(
      String ruc, int modo) {
    List<CustomerPendingApplicationDocumentDto> lista = new ArrayList<>();
    try {
      MapSqlParameterSource in =
          new MapSqlParameterSource()
              .addValue("modo", modo)
              .addValue("fecpro", null, Types.TIMESTAMP)
              .addValue("idcliente", ruc)
              .addValue("idvendedor", null, Types.VARCHAR)
              .addValue("documento", null, Types.VARCHAR)
              .addValue("numcobro", null, Types.VARCHAR)
              .addValue("factura", null, Types.VARCHAR)
              .addValue("fecini", null, Types.TIMESTAMP)
              .addValue("saldofac", null, Types.DECIMAL)
              .addValue("userid", "wsrvmc")
              .addValue("equipo", "wserver");

      Map<String, Object> out = spConsultaDocSaldo.execute(in);

      @SuppressWarnings("unchecked")
      List<Map<String, Object>> rows = (List<Map<String, Object>>) out.get("#result-set-1");

      if (rows == null || rows.isEmpty()) {
        return new ArrayList<>();
      }

      List<CustomerPendingApplicationDocumentDto> result = new ArrayList<>();

      for (Map<String, Object> row : rows) {
        result.add(mapCustomerPendingApplicationDocument(row));
      }

      return result;

    } catch (Exception e) {
      throw new RuntimeException(
          "Error consultando documentos pendientes de aplicación. Cliente="
              + ruc
              + ", modo="
              + modo
              + ": "
              + e.getMessage(),
          e);
    }
  }

  @Override
  public List<CustomerStatusAccountDto> executeSpGetCustomerStatusAccount(String ruc, int modo) {
    try {
      MapSqlParameterSource in =
          new MapSqlParameterSource()
              .addValue("modo", modo)
              .addValue("fecpro", null, Types.TIMESTAMP)
              .addValue("idcliente", ruc)
              .addValue("idvendedor", null, Types.VARCHAR)
              .addValue("documento", null, Types.VARCHAR)
              .addValue("numcobro", null, Types.VARCHAR)
              .addValue("factura", null, Types.VARCHAR)
              .addValue("fecini", null, Types.TIMESTAMP)
              .addValue("saldofac", null, Types.DECIMAL)
              .addValue("userid", "wsrvmc")
              .addValue("equipo", "wserver");

      Map<String, Object> out = spConsultaDocSaldo.execute(in);

      @SuppressWarnings("unchecked")
      List<Map<String, Object>> rows = (List<Map<String, Object>>) out.get("#result-set-1");

      if (rows == null || rows.isEmpty()) {
        return new ArrayList<>();
      }

      List<CustomerStatusAccountDto> result = new ArrayList<>();

      for (Map<String, Object> row : rows) {
        result.add(mapCustomerStatusAccountDocuments(row));
      }

      return result;

    } catch (Exception e) {
      throw new RuntimeException(
          "Error consultando documentos pendientes de aplicación. Cliente="
              + ruc
              + ", modo="
              + modo
              + ": "
              + e.getMessage(),
          e);
    }
  }

  @Override
  public CustomerResumenAccountDto executeSpGetCustomerResumenAccount(String ruc, int modo) {
    MapSqlParameterSource in =
        new MapSqlParameterSource()
            .addValue("modo", modo)
            .addValue("fecpro", "")
            .addValue("idcliente", ruc)
            .addValue("idvendedor", "")
            .addValue("documento", "")
            .addValue("factura", "")
            .addValue("numcobro", "")
            .addValue("fecini", "")
            .addValue("saldofac", null)
            .addValue("userid", "wsrvmc")
            .addValue("equipo", "wserver")
            .addValue("codret", null)
            .addValue("msj", "");

    try {
    Map<String, Object> out = spConsultaDocSaldo.execute(in);
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> rows = (List<Map<String, Object>>) out.get("#result-set-1");  
     
      CustomerResumenAccountDto e = new CustomerResumenAccountDto();
      Map<String, Object> row = rows.get(0);

      e.setIdentificacion(CoerceHelperUtils.stringOrEmpty(row.get("Identificacion")));
      e.setActual(CoerceHelperUtils.toBigDecimal(row.get("actual")));
      e.setDias030(CoerceHelperUtils.toBigDecimal(row.get("0_30")));
      e.setDias3160(CoerceHelperUtils.toBigDecimal(row.get("31_60")));
      e.setDias6190(CoerceHelperUtils.toBigDecimal(row.get("61_90")));
      e.setDias91Mas(CoerceHelperUtils.toBigDecimal(row.get("91_mas")));
      e.setTotal(CoerceHelperUtils.toBigDecimal(row.get("total")));
      return e ;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private CustomerPendingApplicationDocumentDto mapCustomerPendingApplicationDocument(
      Map<String, Object> row) {

    CustomerPendingApplicationDocumentDto dto = new CustomerPendingApplicationDocumentDto();

    dto.setIdentificacion(CoerceHelperUtils.stringOrEmpty(row.get("Identificacion")));
    dto.setDocumento(CoerceHelperUtils.stringOrEmpty(row.get("Documento")));
    dto.setTipo(CoerceHelperUtils.stringOrEmpty(row.get("Tipo")));
    dto.setObservacion(CoerceHelperUtils.stringOrEmpty(row.get("Observacion")));
    dto.setFechaEmision(CoerceHelperUtils.toLocalDate(row.get("Fecha_Emision")));
    dto.setMontoOriginal(CoerceHelperUtils.toBigDecimal(row.get("Monto_Original")));
    dto.setSaldoCliente(CoerceHelperUtils.toBigDecimal(row.get("Saldo_Cliente")));

    return dto;
  }

  private CustomerStatusAccountDto mapCustomerStatusAccountDocuments(Map<String, Object> row) {
    CustomerStatusAccountDto dto = new CustomerStatusAccountDto();
    dto.setIdentificacion(CoerceHelperUtils.stringOrEmpty(row.get("Identificacion")));
    dto.setTipo(CoerceHelperUtils.stringOrEmpty(row.get("Tipo")));
    dto.setDocumento(CoerceHelperUtils.stringOrEmpty(row.get("Documento")));
    dto.setFechaEmision(CoerceHelperUtils.toLocalDate(row.get("Fecha_Emision")));
    dto.setFechaVencimiento(CoerceHelperUtils.toLocalDate(row.get("Fecha_Vencimiento")));
    dto.setCondicionPago(CoerceHelperUtils.stringOrEmpty(row.get("Condicion_Pago")));
    dto.setMontoOriginal(CoerceHelperUtils.toBigDecimal(row.get("Monto_Original")));
    dto.setAbono(CoerceHelperUtils.toBigDecimal(row.get("Abono")));
    dto.setSaldoPendiente(CoerceHelperUtils.toBigDecimal(row.get("Saldo_Pendiente")));
    dto.setDias(CoerceHelperUtils.toInt(row.get("Dias"), 0));
    dto.setObservacion(CoerceHelperUtils.stringOrEmpty(row.get("Observacion")));
    return dto;
  }
}
