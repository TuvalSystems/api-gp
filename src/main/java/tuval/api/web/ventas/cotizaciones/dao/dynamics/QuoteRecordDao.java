package tuval.api.web.ventas.cotizaciones.dao.dynamics;

import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import tuval.api.web.ventas.cotizaciones.dto.ItemDescripcionAlternativaDto;
import tuval.api.web.ventas.cotizaciones.dto.SalesProjectReasonDto;
import tuval.api.web.ventas.cotizaciones.entity.dynamics.QuoteRecordEntity;

@Repository
public class QuoteRecordDao {
  private final JdbcTemplate jdbcTemplate;

  public QuoteRecordDao(@Qualifier("dynamicsJdbcTemplate") JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
    JdbcTemplate dynamicsJdbcTemplate =
        Objects.requireNonNull(jdbcTemplate, "El bean dynamicsJdbcTemplate no puede ser null");
  }

  public void save(QuoteRecordEntity entity) {
    int updatedRows = update(entity);
    if (updatedRows == 0) {
      insert(entity);
    }
  }

  private int update(QuoteRecordEntity entity) {
    String sql =
        """
            UPDATE dbo.tbm_cabcotrec
            SET tx_usr = ?,
                tx_cliRuc = ?,
                tx_cliRazSoc = ?,
                tx_cotCliSri = ?,
                tx_est = ?,
                tx_bod = ?,
                tx_ptoVta = ?,
                tx_ptoVtaDesc = ?,
                tx_cotReqapr = ?,
                tx_cotAprCom = ?,
                tx_cotMot = ?,
                co_cotRec = ?,
                fe_cotRec = ?,
                tx_obsMot = ?,
                fe_ultmod = ?,
                tx_usrMod = ?,
                nd_cotTot = ?
            WHERE tx_cot = ?
        """;

    return jdbcTemplate.update(
        sql,
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
        entity.getFeUltmod(),
        entity.getTxUsrMod(),
        entity.getNdCotTot(),
        entity.getTxCot());
  }

  private void insert(QuoteRecordEntity entity) {
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
  public List<SalesProjectReasonDto> findAllSalesProjectReasons() {
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

    return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(SalesProjectReasonDto.class));
  }

  /** RETORNA UN MOTIVO / PROYECTO DE VENTA POR CODIGO */
  public SalesProjectReasonDto findSalesProjectReasonByCoMotproven(Integer coMotproven) {
    String sql =
        """
        SELECT
          co_motproven AS coMotproven,
          RTRIM(tx_descor) AS txDescor,
          RTRIM(tx_deslar) AS txDeslar,
          RTRIM(tx_obs) AS txObs
        FROM dbo.vw_listadoMotivosProyectosVenta
        WHERE co_motproven = ?
        """;

    List<SalesProjectReasonDto> result =
        jdbcTemplate.query(
            sql, new BeanPropertyRowMapper<>(SalesProjectReasonDto.class), coMotproven);

    return result.isEmpty() ? null : result.get(0);
  }
}
