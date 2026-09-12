package tuval.api.web.bodega.reservas.dao.dynamics;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import tuval.api.web.bodega.reservas.entity.dynamics.NotifyLocalEntity;

@Repository
public class ReservedDao {

  @PostConstruct
  public void init() {
    validateConfiguration();
  }

  @Autowired
  @Qualifier("dynamicsJdbcTemplate")
  private JdbcTemplate jdbcTemplate;

  public void validateConfiguration() {
    if (jdbcTemplate == null) {
      throw new IllegalStateException("JdbcTemplate no ha sido inyectado correctamente");
    }
  }

  public NotifyLocalEntity findByTxPtoVta(String ptovta, int conot) {
    String sql =
        """
           SELECT
              co_emp          AS coEmp,
              co_loc          AS coLoc,
              tx_ptoVta       AS txPtoVta,
              co_not          AS coNot,
              tx_dirsercor    AS txDirSerCor,
              ne_puesercor    AS nePueSerCor,
              tx_corelerem    AS txCoreLeRem,
              tx_coreleclarem AS txCoreLeClaRem,
              tx_coreledes    AS txCoreLeDes,
              tx_corelecop    AS txCoreLeCop,
              tx_asu          AS txAsu,
              st_reg          AS stReg
          FROM dbo.vw_notificacionesReservasLocal
          WHERE tx_ptoVta = ? and co_not = ?
        """;

    try {
      var mapper =
          new org.springframework.jdbc.core.BeanPropertyRowMapper<>(NotifyLocalEntity.class);
      return jdbcTemplate.queryForObject(sql, mapper, ptovta, conot);
    } catch (EmptyResultDataAccessException e) {
      throw new IllegalStateException(
          "No se encontró registro para tx_ptoVta=" + ptovta + ", co_not=" + conot, e);
    } catch (IncorrectResultSizeDataAccessException e) {
      throw new IllegalStateException(
          "Se encontraron múltiples registros (esperaba 1) para tx_ptoVta="
              + ptovta
              + ", co_not="
              + conot,
          e);
    }
  }
}
