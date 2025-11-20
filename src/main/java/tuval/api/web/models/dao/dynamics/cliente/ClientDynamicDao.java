package tuval.api.web.models.dao.dynamics.cliente;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import tuval.api.web.models.dto.cliente.SellerRequest;
import tuval.api.web.models.entity.dynamics.cliente.ListSegmentClientEntity;
import tuval.api.web.models.entity.dynamics.cliente.ListSellerEntity;
import tuval.api.web.models.entity.dynamics.cliente.TypeSellerEntity;

@Repository
public class ClientDynamicDao {
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

  public ListSellerEntity findByTxUsr(SellerRequest req) {
    String SQL =
        """
        SELECT co_usr as coUsr, tx_usr  as txUsr, co_tipven as coTipVen
        FROM dbo.vw_listadoVendedoresWeb
        WHERE tx_usr = ?
        """;
    try {
      var mapper =
          new org.springframework.jdbc.core.BeanPropertyRowMapper<>(ListSellerEntity.class);
      return jdbcTemplate.queryForObject(SQL, mapper, req.getUsuario());
    } catch (Exception e) {
      throw new IllegalStateException("Ocurrio un error al consultar la base de datos", e);
    }
  }

  public ListSegmentClientEntity findSegmentByTxCli(String txCli) {
    String SQL =
        """
        SELECT TOP (1)
          co_atrPadre      AS coAtrPadre,
          segmento         AS segmento,
          observacionPadre AS observacionPadre,
          co_atrHijo       AS coAtrHijo,
          subsegmento      AS subsegmento,
          observacionHijo  AS observacionHijo,
          tx_cli           AS txCli
        FROM dbo.vw_clientesPorSegmento
        WHERE tx_cli = ?
        ORDER BY co_atrHijo DESC

        """;
    try {
      var mapper =
          new org.springframework.jdbc.core.BeanPropertyRowMapper<>(ListSegmentClientEntity.class);
      return jdbcTemplate.queryForObject(SQL, mapper, txCli);
    } catch (org.springframework.dao.EmptyResultDataAccessException e) {
      // No hay filas para ese tx_cli
      throw new IllegalStateException("No existe segmento para tx_cli=" + txCli, e);
    } catch (org.springframework.dao.IncorrectResultSizeDataAccessException e) {
      // No debería pasar con TOP(1), pero lo capturamos por seguridad
      throw new IllegalStateException("Se obtuvo más de un resultado para tx_cli=" + txCli, e);
    } catch (org.springframework.dao.DataAccessException e) {
      throw new IllegalStateException("Ocurrió un error al consultar la base de datos", e);
    }
  }

  public TypeSellerEntity getTypeSellerForTxUsr(String username) {
    String SQL =
        """
        SELECT co_usr as coUsr, tx_usr  as txUsr, co_tipven as coTipVen, LTRIM(RTRIM(tx_descor)) as txDescor
        FROM dbo.vw_Vendedores_TipVen
        WHERE tx_usr = ?
        """;
    try {
      var mapper =
          new org.springframework.jdbc.core.BeanPropertyRowMapper<>(TypeSellerEntity.class);
      return jdbcTemplate.queryForObject(SQL, mapper, username);
    } catch (Exception e) {
      throw new IllegalStateException("Ocurrio un error al consultar la base de datos", e);
    }
  }
}
