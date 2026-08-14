package tuval.api.web.models.dao.dynamics.cotizaciones;

import jakarta.annotation.PostConstruct;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import tuval.api.web.models.dto.cotizaciones.ItemDescripcionAlternativaDto;
import tuval.api.web.models.dto.cotizaciones.MotivoProyectoVentaDto;
import tuval.api.web.models.entity.dynamics.cotizaciones.QuoteRecordEntity;

@Repository
public class QuoteRecordDao {

  private static final Logger log = LoggerFactory.getLogger(QuoteRecordDao.class);

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

  // @Autowired
  // private JdbcTemplate dynamicsJdbcTemplate;
  public void save(QuoteRecordEntity entity) {
    String sql =
        """
            INSERT INTO dbo.tbm_cabcotrec (
                tx_cot, tx_usr, tx_cliRuc, tx_cliRazSoc, tx_cotCliSri, tx_est,
                tx_bod, tx_ptoVta, tx_ptoVtaDesc, tx_cotReqapr, tx_cotAprCom,
                tx_cotMot, co_cotRec, fe_cotRec, tx_obsMot, fe_ing,
                tx_usrIng, fe_ultmod, tx_usrMod, nd_cotTot
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

    jdbcTemplate.update(
        sql,
        entity.getTxCot(),
        entity.getTxUsr(),
        entity.getTxCliRuc(),
        entity.getTxCliRazSoc(),
        entity.getTxCotCliSri(),
        entity.getTxEst(),
        entity.getTxBod(),
        entity.getTxPtoVta(),
        entity.getTxPtoVtaDesc(),
        entity.getTxCotReqapr(),
        entity.getTxCotAprCom(),
        entity.getTxCotMot(),
        entity.getCoCotRec(),
        entity.getFeCotRec(),
        entity.getTxObsMot(),
        entity.getFeIng(),
        entity.getTxUsrIng(),
        entity.getFeUltmod(),
        entity.getTxUsrMod(),
        entity.getNdCotTot());
  }

  /** RETORNA LISTA DE ITEMS ALTERNATIVOS */
  public List<ItemDescripcionAlternativaDto> findByItem(String item) {
    String sql = "SELECT * FROM vw_ListadoItemsDescripcionesAlternativas WHERE tx_itm = ?";

    return jdbcTemplate.query(
        sql, new BeanPropertyRowMapper<>(ItemDescripcionAlternativaDto.class), item);
  }

  /** RETORNA LISTA DE MOTIVOS / PROYECTOS DE VENTA */
  public List<MotivoProyectoVentaDto> findAllMotivosProyectosVenta() {
    String sql =
        """
        SELECT
          co_motproven AS coMotproven,
          RTRIM(tx_descor) AS txDescor,
          RTRIM(tx_deslar) AS txDeslar,
          RTRIM(tx_obs) AS txObs
        FROM dbo.vw_listadoMotivosProyectosVenta
        ORDER BY co_motproven ASC
        """;

    return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(MotivoProyectoVentaDto.class));
  }
}
