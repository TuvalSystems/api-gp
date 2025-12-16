package tuval.api.web.models.dao.dynamics.cliente;

import jakarta.annotation.PostConstruct;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import tuval.api.web.models.dto.cliente.ListAllSegmentsDto;
import tuval.api.web.models.dto.cliente.ListAllSubsegmentsDto;
import tuval.api.web.models.dto.cliente.ListSegmentClientDto;
import tuval.api.web.models.dto.cliente.ListSellerDto;
import tuval.api.web.models.dto.cliente.TypeSellerDto;
import tuval.api.web.models.request.cliente.ClientSubsegmentRucRequest;
import tuval.api.web.models.request.cliente.SellerRequest;

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

  public ListSellerDto findByTxUsr(SellerRequest req) {
    String SQL =
        """
        SELECT co_usr as coUsr, tx_usr  as txUsr, co_tipven as coTipVen
        FROM dbo.vw_listadoVendedoresWeb
        WHERE tx_usr = ?
        """;
    try {
      var mapper =
          new org.springframework.jdbc.core.BeanPropertyRowMapper<>(ListSellerDto.class);
      return jdbcTemplate.queryForObject(SQL, mapper, req.getUsuario());
    } catch (Exception e) {
      throw new IllegalStateException("Ocurrio un error al consultar la base de datos", e);
    }
  }

  /** funcion para obtener todos los segmentos */
  public List<ListAllSegmentsDto> findAllSegment() {
    String SQL =
        """
        SELECT DISTINCT
            TRIM(segmento) AS segmento,
            co_atrPadre AS coAtrPadre
        FROM dbo.vw_segmentosClientes
        ORDER BY segmento ASC
        """;
    try {
      var mapper =
          new org.springframework.jdbc.core.BeanPropertyRowMapper<>(ListAllSegmentsDto.class);
      return jdbcTemplate.query(SQL, mapper);
    } catch (Exception e) {
      throw new IllegalStateException("Ocurrio un error al consultar la base de datos", e);
    }
  }

  public List<ListAllSubsegmentsDto> findAllSubSegmentsBySegment(int coAtrPadre) {
    String SQL =
        """
        SELECT DISTINCT
            TRIM(subsegmento) AS subsegmento,
            co_atrHijo AS coAtrHijo
        FROM dbo.vw_segmentosClientes
        WHERE co_atrPadre = ?
        ORDER BY subsegmento ASC
        """;
    try {
      var mapper =
          new org.springframework.jdbc.core.BeanPropertyRowMapper<>(ListAllSubsegmentsDto.class);
      return jdbcTemplate.query(SQL, mapper, coAtrPadre);
    } catch (Exception e) {
      throw new IllegalStateException("Ocurrio un error al consultar la base de datos", e);
    }
  }

  public ListSegmentClientDto findSegmentByTxCli(String txCli) {
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
          new org.springframework.jdbc.core.BeanPropertyRowMapper<>(ListSegmentClientDto.class);
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

  public TypeSellerDto getTypeSellerForTxUsr(String username) {
    String SQL =
        """
        SELECT co_usr as coUsr, tx_usr  as txUsr, co_tipven as coTipVen, LTRIM(RTRIM(tx_descor)) as txDescor, territorio
        FROM dbo.vw_Vendedores_TipVen
        WHERE tx_usr = ?
        """;
    try {
      var mapper =
          new org.springframework.jdbc.core.BeanPropertyRowMapper<>(TypeSellerDto.class);
      return jdbcTemplate.queryForObject(SQL, mapper, username);
    } catch (Exception e) {
      throw new IllegalStateException("Ocurrio un error al consultar la base de datos", e);
    }
  }

  public void insertClientSubSegmentAndRuc(ClientSubsegmentRucRequest req) {
    String sql = "INSERT INTO tbr_atrcli (tx_cli, co_atrCli) VALUES (?, ?)";
    try {
      int filasAfectadas = jdbcTemplate.update(sql, req.getRuc(), req.getSubsegmentoId());
      if (filasAfectadas == 0) {
        throw new RuntimeException("No se insertó ningún registro en tbr_atrcli");
      }
    } catch (DataAccessException e) {

      throw new RuntimeException("Error crítico insertando segmento: " + e.getMessage(), e);
    }
  }
}
