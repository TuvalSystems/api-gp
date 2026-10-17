package tuval.api.web.clientes.dao.dynamics;

import java.sql.Types;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import tuval.api.web.clientes.dto.ListAllSegmentsDto;
import tuval.api.web.clientes.dto.ListAllSubsegmentsDto;
import tuval.api.web.clientes.dto.ListSegmentClientDto;
import tuval.api.web.clientes.dto.ListSellerDto;
import tuval.api.web.clientes.dto.TypeSellerDto;
import tuval.api.web.clientes.request.ClientBranchRequets;
import tuval.api.web.clientes.request.BranchConfigSyncKeyRequest;
import tuval.api.web.clientes.request.ClientSubsegmentRucRequest;
import tuval.api.web.clientes.request.SellerRequest;

@Repository
public class ClientDynamicDao {

  private final JdbcTemplate jdbcTemplate;
  private final SimpleJdbcCall spUpdateinfoSucursal;


   public ClientDynamicDao(@Qualifier("dynamicsJdbcTemplate") JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
      this.spUpdateinfoSucursal = new SimpleJdbcCall(jdbcTemplate)
        .withSchemaName("dbo")
        .withProcedureName("sp_actDirCli")
        .withoutProcedureColumnMetaDataAccess()
        .declareParameters(
            new SqlParameter("modo", Types.SMALLINT),
            new SqlParameter("CUSTNMBR", Types.CHAR),
            new SqlParameter("ADRSCODE", Types.CHAR),
            new SqlParameter("ADDRESS1", Types.CHAR),
            new SqlParameter("st_reg", Types.CHAR),
            new SqlParameter("tx_usring", Types.NVARCHAR),
            new SqlParameter("fe_ing", Types.TIMESTAMP),
            new SqlParameter("tx_usrmod", Types.NVARCHAR),
            new SqlParameter("fe_ultmod", Types.TIMESTAMP),
            new SqlOutParameter("codret", Types.INTEGER),
            new SqlOutParameter("msj", Types.VARCHAR));
   }



  public List<tuval.api.web.clientes.dto.PendingBranchConfigSyncDto> findPendingBranchConfigurations(int limit, BranchConfigSyncKeyRequest cursor) {
    if (limit < 1 || limit > 1000) {
      throw new IllegalArgumentException("El limite debe estar entre 1 y 1000");
    }
    String sql = """
        SELECT TOP (?) co_emp, co_loc, LTRIM(RTRIM(tx_cli)) AS tx_cli,
            co_ven, fe_ultmod, co_usrmod,
            LTRIM(RTRIM(st_cligencom)) AS st_cligencom,
            LTRIM(RTRIM(st_solresantfec)) AS st_solresantfec,
            LTRIM(RTRIM(st_solressinfec)) AS st_solressinfec,
            LTRIM(RTRIM(st_tipVen)) AS st_tipVen, st_sinMySql
        FROM [Dynamics2021].[dbo].[tbr_cliloc]
        WHERE st_sinMySql = 1
        """;
    if (cursor != null) {
      if (cursor.getCoEmp() == null || cursor.getCoLoc() == null
          || cursor.getTxCli() == null || cursor.getTxCli().isBlank()) {
        throw new IllegalArgumentException("El cursor requiere coEmp, coLoc y txCli");
      }
      sql += """
          AND (co_emp > ?
              OR (co_emp = ? AND co_loc > ?)
              OR (co_emp = ? AND co_loc = ? AND LTRIM(RTRIM(tx_cli)) > ?))
          """;
    }
    sql += " ORDER BY co_emp, co_loc, LTRIM(RTRIM(tx_cli))";
    try {
      var mapper = new org.springframework.jdbc.core.BeanPropertyRowMapper<>(
          tuval.api.web.clientes.dto.PendingBranchConfigSyncDto.class);
      if (cursor == null) {
        return jdbcTemplate.query(sql, mapper, limit);
      }
      return jdbcTemplate.query(sql, mapper, limit,
          cursor.getCoEmp(), cursor.getCoEmp(), cursor.getCoLoc(),
          cursor.getCoEmp(), cursor.getCoLoc(), cursor.getTxCli().trim());
    } catch (DataAccessException e) {
      throw new IllegalStateException("Error al consultar configuraciones pendientes de tbr_cliloc", e);
    }
  }

  public int markBranchConfigurationSynced(BranchConfigSyncKeyRequest request) {
    String sql = """
        UPDATE [Dynamics2021].[dbo].[tbr_cliloc]
        SET st_sinMySql = 0
        WHERE co_emp = ? AND co_loc = ? AND tx_cli = ?
        """;
    try {
      int affectedRows = jdbcTemplate.update(sql, request.getCoEmp(), request.getCoLoc(),
          request.getTxCli().trim());
      if (affectedRows == 0) {
        throw new IllegalStateException("No existe configuracion de sucursal para los filtros enviados");
      }
      return affectedRows;
    } catch (DataAccessException e) {
      throw new IllegalStateException("Error al actualizar st_sinMySql de tbr_cliloc", e);
    }
  }

  public int insertBranchConfigurationHistory(BranchConfigSyncKeyRequest request) {
    String sql = """
        INSERT INTO [Dynamics2021].[dbo].[tbh_cliLoc]
            (co_his, co_emp, co_loc, tx_cli, co_ven, fe_ultmod, co_usrmod,
             st_cligencom, st_solresantfec, st_solressinfec, st_tipVen, st_sinMySql)
        SELECT (SELECT COALESCE(MAX(CONVERT(int, history.co_his)), 0) + 1
                    FROM [Dynamics2021].[dbo].[tbh_cliLoc] AS history WITH (TABLOCKX, HOLDLOCK)
                    WHERE history.co_emp = config.co_emp
                      AND history.co_loc = config.co_loc
                      AND history.tx_cli = config.tx_cli
                      AND (history.co_ven = config.co_ven
                           OR (history.co_ven IS NULL AND config.co_ven IS NULL))),
            config.co_emp, config.co_loc, config.tx_cli, config.co_ven,
            config.fe_ultmod, config.co_usrmod, config.st_cligencom,
            config.st_solresantfec, config.st_solressinfec,
            config.st_tipVen, config.st_sinMySql
        FROM [Dynamics2021].[dbo].[tbr_cliloc] AS config
        WHERE config.co_emp = ? AND config.co_loc = ? AND config.tx_cli = ?
        """;
    try {
      int affectedRows = jdbcTemplate.update(sql, request.getCoEmp(), request.getCoLoc(),
          request.getTxCli().trim());
      if (affectedRows == 0) {
        throw new IllegalStateException("No existe configuracion de sucursal para guardar en el historico");
      }
      return affectedRows;
    } catch (DataAccessException e) {
      throw new IllegalStateException("Error al insertar configuracion en tbh_cliLoc: "
          + e.getMostSpecificCause().getMessage(), e);
    }
  }

  public long countClientBranchesPendingMySqlSync() {
    String sql = """
        SELECT COUNT_BIG(*)
        FROM [Dynamics2021].[dbo].[tbr_cliloc]
        WHERE st_sinMySql = 1
        """;
    try {
      return jdbcTemplate.queryForObject(sql, Long.class);
    } catch (DataAccessException e) {
      throw new IllegalStateException("Ocurrio un error al consultar el contador de tbr_cliloc", e);
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

   public void executeSpUpdateStatusBranchClient(ClientBranchRequets req) {
    try {
      // 1. Preparamos los parámetros (Asegúrate de que los nombres coincidan con
      // declareParameters)
      MapSqlParameterSource in = new MapSqlParameterSource()
          .addValue("modo", req.getModo())
          .addValue("CUSTNMBR", req.getRuc())
          .addValue("ADRSCODE", req.getIdDireccion())
          .addValue("ADDRESS1", req.getDireccion())
          .addValue("st_reg", req.getEstado())
          .addValue("tx_usring", req.getUsuarioIngreso())
          .addValue("fe_ing", req.getFechaIngreso())
          .addValue("tx_usrmod", req.getUsuarioModificacion())
          .addValue("fe_ultmod", req.getFechaModificacion());

      // 2. Ejecución
      Map<String, Object> out = spUpdateinfoSucursal.execute(in);

      System.out.println("CONTENIDO REAL DE OUT: " + out);

      Integer codRet = 0;
      String mensaje = "Operación realizada (Sin mensaje)";

      for (Map.Entry<String, Object> entry : out.entrySet()) {
        String key = entry.getKey().toLowerCase();
        if (key.contains("codret")) {
          codRet = (entry.getValue() != null) ? ((Number) entry.getValue()).intValue() : 0;
        } else if (key.contains("msj")) {
          mensaje = (entry.getValue() != null) ? entry.getValue().toString() : mensaje;
        }
      }

      if (codRet != 0) {
        throw new RuntimeException("Error del SP [" + codRet + "]: " + mensaje);
      }

      System.out.println("ÉXITO: " + mensaje);

    } catch (RuntimeException e) {
      // Lanzamos el error de negocio para que el Controller lo atrape
      throw e;
    } catch (Exception e) {
      // Capturamos errores de red, SQL o casting
      throw new RuntimeException("Fallo técnico en la base de datos: " + e.getMessage());
    }
  }



 
}
