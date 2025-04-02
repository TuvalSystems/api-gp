package tuval.api.web.models.dao.tuval.sucursales;

import java.sql.Date;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import tuval.api.web.models.entity.tuval.sucursales.BranchesClientAddressEntity;
import tuval.api.web.models.entity.tuval.sucursales.BranchesClientEntity;
import tuval.api.web.models.entity.tuval.sucursales.BranchesClientsResponseEntity;
import tuval.api.web.models.entity.tuval.sucursales.CoordinateListEntity;
import tuval.api.web.models.entity.tuval.sucursales.CoordinateListResponseEntity;
import tuval.api.web.models.service.tuval.sucursales.utils.BranchesClientsStringProcessUtils;

@Repository
public class BranchesClientsDao {

    @PostConstruct
    public void init() {
        validateConfiguration();
    }
    @Autowired
    @Qualifier("tuvalJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    public void validateConfiguration() {
        if (jdbcTemplate == null) {
            throw new IllegalStateException("JdbcTemplate no ha sido inyectado correctamente");
        }
    }

    public BranchesClientsResponseEntity consultBranchClientList(String cliente, Date fechacreacion,
            Date fechamodificacion) {
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("MSW_SP_OBTENER_CLIENTE_SUCURSAL")
                    .declareParameters(
                            new SqlParameter("@CLIENTE", Types.VARCHAR),
                            new SqlParameter("@FECHA_CREACION", Types.VARCHAR),
                            new SqlParameter("@FECHA_MODI", Types.VARCHAR))
                    .returningResultSet("result", (rs, numRow) -> {
                        BranchesClientEntity entity = new BranchesClientEntity();
                        entity.setCliente(rs.getString("CLIENTE") != null ? rs.getString("CLIENTE").trim() : "");
                        entity.setDescripcion(
                                rs.getString("DESCRIPCION_CLIENTE") != null ? rs.getString("DESCRIPCION_CLIENTE").trim()
                                        : "");

                        String direccionStr = rs.getString("DIRECCIONES") != null ? rs.getString("DIRECCIONES").trim()
                                : "";
                        List<BranchesClientAddressEntity> direcciones = BranchesClientsStringProcessUtils
                                .processAddressStrings(direccionStr);
                        entity.setDirecciones(direcciones);
                        return entity;
                    });

            jdbcCall.getJdbcTemplate().setMaxRows(0); // Ensure no limit on rows returned

            Map<String, Object> params = new HashMap<>();
            params.put("@CLIENTE", cliente != null ? cliente : "");
            params.put("@FECHA_CREACION", fechacreacion != null ? fechacreacion : null);
            params.put("@FECHA_MODI", fechamodificacion != null ? fechamodificacion : null);

            Map<String, Object> result = jdbcCall.execute(params);
            BranchesClientsResponseEntity respEntityData = new BranchesClientsResponseEntity();
            @SuppressWarnings("unchecked")
            List<BranchesClientEntity> data = (List<BranchesClientEntity>) result.get("result");
            respEntityData.setData(data);
            return respEntityData;

        } catch (Exception e) {
            throw new RuntimeException("Error al consultar la lista de sucursales clientes: " + e.getMessage(), e);
        }
    }

    public CoordinateListResponseEntity getCoordinateListSp() {

        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
            .withProcedureName("MSW_SP_DATOS_COORDENADAS")
            .declareParameters(new SqlParameter[] {}).returningResultSet(
                    "result",
                    new BeanPropertyRowMapper<>(CoordinateListEntity.class));

            Map<String, Object> params = new HashMap<>();
            Map<String, Object> result = jdbcCall.execute(params);
           CoordinateListResponseEntity respEntityData = new CoordinateListResponseEntity();
            @SuppressWarnings("unchecked")
            List<CoordinateListEntity> data = (List<CoordinateListEntity>) result.get("result");
            if (data != null) {
                data.forEach(entity -> {
                    entity.setIdCliente(entity.getIdCliente() != null ? entity.getIdCliente().trim() : "");
                    entity.setAddressCode(entity.getAddressCode() != null ? entity.getAddressCode().trim() : "");
                    entity.setDatosCoordenadas(entity.getDatosCoordenadas() != null ? entity.getDatosCoordenadas().trim() : "");
                });
            }
            respEntityData.setData(data);
            return respEntityData;
        } catch (Exception e) {
            throw new RuntimeException("Error al consultar transferencias: " + e.getMessage(), e);
        }
       
    }

}
