package tuval.api.web.models.dao.tuval.cobros.Imp;

import jakarta.annotation.PostConstruct;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import tuval.api.web.models.Utils.CoerceHelperUtils;
import tuval.api.web.models.dao.tuval.cobros.CreditCheckDao;
import tuval.api.web.models.entity.tuval.cobros.CheckValidatorEntity;
import tuval.api.web.models.entity.tuval.cobros.ClientOverdueDocumentEntity;

@Repository
public class CreditCheckDaoImp implements CreditCheckDao {

  @Autowired
  @Qualifier("tuvalJdbcTemplate")
  private JdbcTemplate jdbcTemplate;

  /** variables globales para el manejo de sp */
  private SimpleJdbcCall spValidacionCrediticia;

  public void validateConfiguration() {
    if (jdbcTemplate == null) {
      throw new IllegalStateException("JdbcTemplate no ha sido inyectado correctamente");
    }
  }

  @PostConstruct
  public void init() {
    validateConfiguration();
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
  }

  public ClientOverdueDocumentEntity executeSpConsultExpiredClientDocument(
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
      System.out.println(row + " - " + row.get("RUC") + "executeSpConsultExpiredClientDocument");
      ClientOverdueDocumentEntity e = new ClientOverdueDocumentEntity();
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

  public CheckValidatorEntity getConsultCreditValidation(String ruc, String idCompania, int modo) {
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
      CheckValidatorEntity e = new CheckValidatorEntity();
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
}
