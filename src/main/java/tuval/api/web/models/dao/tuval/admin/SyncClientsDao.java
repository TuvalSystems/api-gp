package tuval.api.web.models.dao.tuval.admin;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import tuval.api.web.models.Utils.CoerceHelperUtils;
import tuval.api.web.models.Utils.SyncClientsBranchesUtils;
import tuval.api.web.models.dto.admin.SyncClientAddressDetailDto;
import tuval.api.web.models.dto.admin.SyncClientBranchesDto;
import tuval.api.web.models.dto.admin.SyncClientCoordinatesDto;
import tuval.api.web.models.dto.admin.SyncClientsDto;
import tuval.api.web.models.request.admin.SyncClientsRequest;

@Repository
public class SyncClientsDao {

  private final JdbcTemplate jdbcTemplate;
  private final SimpleJdbcCall spSyncClients;
  private final SimpleJdbcCall spSyncClientsBranches;
  private final SimpleJdbcCall spSyncClientsCoordinates;

  public SyncClientsDao(@Qualifier("tuvalJdbcTemplate") @NonNull JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;

    this.spSyncClients =
        new SimpleJdbcCall(jdbcTemplate)
            .withSchemaName("dbo")
            .withProcedureName("MSW_SP_OBTENER_CLIENTE")
            .withoutProcedureColumnMetaDataAccess()
            .declareParameters(
                new SqlParameter("cliente", Types.VARCHAR),
                new SqlParameter("fecha_creacion", Types.VARCHAR),
                new SqlParameter("fecha_modi", Types.VARCHAR));

    this.spSyncClientsBranches =
        new SimpleJdbcCall(jdbcTemplate)
            .withSchemaName("dbo")
            .withProcedureName("MSW_SP_OBTENER_CLIENTE_SUCURSAL")
            .withoutProcedureColumnMetaDataAccess()
            .declareParameters(
                new SqlParameter("CLIENTE", Types.VARCHAR),
                new SqlParameter("FECHA_CREACION", Types.VARCHAR),
                new SqlParameter("FECHA_MODI", Types.VARCHAR));
    this.spSyncClientsCoordinates = new SimpleJdbcCall(jdbcTemplate)
        .withSchemaName("dbo")
        .withProcedureName("MSW_SP_DATOS_COORDENADAS")
        .withoutProcedureColumnMetaDataAccess()
        .returningResultSet("result", (rs, rowNum) -> {
            SyncClientCoordinatesDto dto = new SyncClientCoordinatesDto();
            dto.setIdCliente(CoerceHelperUtils.stringOrEmpty(rs.getString("Id_Cliente")));
            dto.setAddressCode(CoerceHelperUtils.stringOrEmpty(rs.getString("AddressCode")));
            dto.setDatosCoordenadas(CoerceHelperUtils.stringOrEmpty(rs.getString("DatosCoordenadas")));
            return dto;
        });
  }

  public List<SyncClientsDto> executeSpListSyncClients(SyncClientsRequest request) {

    MapSqlParameterSource in =
        new MapSqlParameterSource()
            .addValue("cliente", request.getCliente())
            .addValue("fecha_creacion", request.getFechaCreacion())
            .addValue("fecha_modi", request.getFechaModif());
    try {
      Map<String, Object> out = spSyncClients.execute(in);

      @SuppressWarnings("unchecked")
      List<Map<String, Object>> rs = (List<Map<String, Object>>) out.get("#result-set-1");

      List<SyncClientsDto> lista = new ArrayList<>();
      for (Map<String, Object> rows : rs) {
        SyncClientsDto e = new SyncClientsDto();
        e.setRuc(CoerceHelperUtils.stringOrEmpty(rows.get("RUC")));
        e.setRazonSocial(CoerceHelperUtils.stringOrEmpty(rows.get("RAZON_SOCIAL")));
        e.setNombreCorto(CoerceHelperUtils.stringOrEmpty(rows.get("NOMBRE_CORTO")));
        e.setIdClase(CoerceHelperUtils.stringOrEmpty(rows.get("ID_CLASE")));
        e.setPrioridad(CoerceHelperUtils.stringOrEmpty(rows.get("PRIORIDAD")));
        e.setDirPrincipal(CoerceHelperUtils.stringOrEmpty(rows.get("DIRECCION_PRINCIPAL")));
        e.setContacto(CoerceHelperUtils.stringOrEmpty(rows.get("CONTACTO")));
        e.setDireccion(CoerceHelperUtils.stringOrEmpty(rows.get("DIRECCION")));
        e.setCiudad(CoerceHelperUtils.stringOrEmpty(rows.get("CIUDAD")));
        e.setProvincia(CoerceHelperUtils.stringOrEmpty(rows.get("PROVINCIA")));
        e.setPais(CoerceHelperUtils.stringOrEmpty(rows.get("PAIS")));
        e.setTerritorio(CoerceHelperUtils.stringOrEmpty(rows.get("TERRITORIO")));
        e.setEstado(CoerceHelperUtils.stringOrEmpty(rows.get("ESTADO")));
        e.setSuspendido(CoerceHelperUtils.stringOrEmpty(rows.get("SUSPENDIDO")));
        e.setTelefono1(CoerceHelperUtils.stringOrEmpty(rows.get("TELEFONO_1")));
        e.setTelefono2(CoerceHelperUtils.stringOrEmpty(rows.get("TELEFONO_2")));
        e.setTelefono3(CoerceHelperUtils.stringOrEmpty(rows.get("TELEFONO_3")));
        e.setTelefono4(CoerceHelperUtils.stringOrEmpty(rows.get("TELEFONO_4")));
        e.setFechaModif(CoerceHelperUtils.toLocalDate(rows.get("FECHA_MODIFICACION")));
        e.setFechaCreacion(CoerceHelperUtils.toLocalDate(rows.get("FECHA_CREACION")));
        e.setEmailSri(CoerceHelperUtils.stringOrEmpty(rows.get("CORREO_ELECTRONICO_SRI")));
        e.setEmailInterno(
            CoerceHelperUtils.stringOrEmpty(rows.get("CORREO_ELECTRONICO_NOTIFICACION_INTERNA")));
        e.setImpVta(CoerceHelperUtils.toBigDecimal(rows.get("IMP_VTA")));
        e.setDirEnvio(CoerceHelperUtils.stringOrEmpty(rows.get("DIRECCION_ENVIO")));
        e.setDirFactura(CoerceHelperUtils.stringOrEmpty(rows.get("DIRECCION_FACTURA")));
        e.setVendedor(CoerceHelperUtils.stringOrEmpty(rows.get("VENDEDOR")));
        e.setDescComercial(CoerceHelperUtils.toBigDecimal(rows.get("DESCUENTO_COMERCIAL")));
        e.setCondPago(CoerceHelperUtils.stringOrEmpty(rows.get("CONDICION_PAGO")));
        e.setDiaGraciaCredito(CoerceHelperUtils.toInt(rows.get("DIA_GRACIA_VTA_CREDITO"), 0));
        e.setDiaGraciaCheque(CoerceHelperUtils.toInt(rows.get("DIA_GRACIA_CHEQUE_FECHA"), 0));
        e.setNivelPrecio(CoerceHelperUtils.stringOrEmpty(rows.get("NIVEL_PRECIO")));
        e.setTipoCredito(CoerceHelperUtils.stringOrEmpty(rows.get("TIPO_CREDITO")));
        e.setMontoCredito(CoerceHelperUtils.toBigDecimal(rows.get("MONTO_CREDITO")));
        e.setGrupoEconomico(CoerceHelperUtils.stringOrEmpty(rows.get("GRUPO_ECONOMICO")));
        e.setGrupo(CoerceHelperUtils.stringOrEmpty(rows.get("GRUPO")));
        e.setSubGrupo(CoerceHelperUtils.stringOrEmpty(rows.get("SUBGRUPO")));
        e.setDiaMaxMesFacturacion(CoerceHelperUtils.toInt(rows.get("DIA_MAXIMO_MES_FACTURACION"), 0));
        e.setFactPostAlDespacho(
            CoerceHelperUtils.toInt(rows.get("FACTURA_POSTERIOR_AL_DESPACHO"), 0));
        e.setRetFte(CoerceHelperUtils.stringOrEmpty(rows.get("RetFte")));
        e.setRetIva(CoerceHelperUtils.stringOrEmpty(rows.get("RetIVA")));
        lista.add(e);
      }
      return lista;
    } catch (Exception e) {
      throw new RuntimeException("SP executeSpListSyncClients falló: " + e.getMessage(), e);
    }
  }

  public List<SyncClientBranchesDto> executeSpListClientsBranchesDao(SyncClientsRequest request) {
    try {
      MapSqlParameterSource in =
          new MapSqlParameterSource()
              .addValue("CLIENTE", request.getCliente())
              .addValue("FECHA_CREACION", request.getFechaCreacion())
              .addValue("FECHA_MODI", request.getFechaModif());

      this.spSyncClientsBranches.returningResultSet(
          "result",
          (rs, rowNum) -> {
            SyncClientBranchesDto dto = new SyncClientBranchesDto();
            dto.setCliente(rs.getString(1) != null ? rs.getString(1).trim() : "");
            dto.setDescripcion(rs.getString(2) != null ? rs.getString(2).trim() : "");

            String rawAddresses = rs.getString(3);
            List<SyncClientAddressDetailDto> direcciones =
                SyncClientsBranchesUtils.processAddressStrings(rawAddresses);
            dto.setDirecciones(direcciones);
            return dto;
          });

      Map<String, Object> out = this.spSyncClientsBranches.execute(in);

      @SuppressWarnings("unchecked")
      List<SyncClientBranchesDto> resultado = (List<SyncClientBranchesDto>) out.get("result");
      return (resultado != null) ? resultado : new ArrayList<>();
    } catch (Exception e) {
      throw new RuntimeException("SP executeSpListClientsBranchesDao falló: " + e.getMessage(), e);
    }
  }



  public List<SyncClientCoordinatesDto> executeSpListClientCoordinates(){
    try {
      Map<String, Object> out = this.spSyncClientsCoordinates.execute();
      @SuppressWarnings("unchecked")
      List<SyncClientCoordinatesDto> resultado = (List<SyncClientCoordinatesDto>) out.get("result");
      return (resultado != null) ? resultado : new ArrayList<>();
    } catch (Exception e) {
      throw new RuntimeException("SP executeSpListClientCoordinates falló: " + e.getMessage(), e);
    }
  }


  
}
