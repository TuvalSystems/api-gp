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
import org.springframework.stereotype.Repository;
import org.springframework.lang.NonNull;

import tuval.api.web.models.Utils.CoerceHelperUtils;
import tuval.api.web.models.dto.admin.SyncClientsDto;
import tuval.api.web.models.request.admin.SyncClientsRequest;

@Repository
public class SyncClientsDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcCall spSyncClients;

    public SyncClientsDao(@Qualifier("tuvalJdbcTemplate") @NonNull JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;

        this.spSyncClients = new SimpleJdbcCall(jdbcTemplate)
                .withSchemaName("dbo")
                .withProcedureName("MSW_SP_OBTENER_CLIENTE")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("cliente", Types.VARCHAR),
                        new SqlParameter("fecha_creacion", Types.VARCHAR),
                        new SqlParameter("fecha_modi", Types.VARCHAR));

    }

    public List<SyncClientsDto> getListSyncClients(SyncClientsRequest request) {

        MapSqlParameterSource in = new MapSqlParameterSource()
                .addValue("cliente", request.getCliente())
                .addValue("fecha_creacion", request.getFechaCreacion())
                .addValue("fecha_modi", request.getFechaModif());

        try {
            Map<String, Object> out = spSyncClients.execute(in);


            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rs = (List<Map<String, Object>>) out.get("#result-set-1");

            List<SyncClientsDto> lista = new ArrayList<>();
            for(Map<String, Object> rows : rs){
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
                e.setEmailInterno(CoerceHelperUtils.stringOrEmpty(rows.get("CORREO_ELECTRONICO_NOTIFICACION_INTERNA")));
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
                e.setDiaMaxMesFacturacion(CoerceHelperUtils.toInt(rows.get("DIA_MAX_MES_FACTURACION"), 0));
                e.setFactPostAlDespacho(CoerceHelperUtils.toInt(rows.get("FACTURA_POSTERIOR_AL_DESPACHO"), 0));
                e.setRetFte(CoerceHelperUtils.stringOrEmpty(rows.get("RetFte")));
                e.setRetIva(CoerceHelperUtils.stringOrEmpty(rows.get("RetIVA")));
                



                lista.add(e);
            }
         return lista;
        } catch (Exception e) {
            throw new RuntimeException("SP executeSpValidateExistsDeliveryNoteResponse falló: " + e.getMessage(), e);
        }
    }

}
