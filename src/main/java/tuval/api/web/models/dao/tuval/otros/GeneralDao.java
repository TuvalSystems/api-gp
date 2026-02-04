package tuval.api.web.models.dao.tuval.otros;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import tuval.api.web.models.dto.otros.pedidos.OrderDetailDto;
import tuval.api.web.models.dto.otros.pedidos.OrderHeaderDto;
import tuval.api.web.models.request.otros.pedidos.OrdersDetailRequest;
import tuval.api.web.models.request.otros.pedidos.OrdersHeadersRequest;

@Repository
public class GeneralDao {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public GeneralDao(
            @Qualifier("tuvalJdbcTemplate") JdbcTemplate jdbcTemplate,
            // Agregamos el Qualifier para asegurar que usa la conexión correcta
            @Qualifier("tuvalNamedParameterJdbcTemplate") NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate);
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public List<OrderHeaderDto> getAllOrderPendingUpdate(Date fechaInicio, Date fechaFin) {
        String SQL = """
                select
                I_vDOCU_ORIGEN as docOrigen,
                I_vID_TIPO as idTipo,
                I_vPUNTO_VENTA as ptoVta,
                DOC_GENERADO as docGenerado,
                DEX_ROW_ID   as dexRowId,
                I_vLOCNCODE as bodega
                from
                MSW_TB_FACTURA_CAB
                WHERE USERID IS NULL
                AND COND_PAGO_ORG IS NULL
                AND I_vDOCDATE BETWEEN ? AND ?
                ORDER BY DEX_ROW_ID
                """;
        try {
            var mapper = new org.springframework.jdbc.core.BeanPropertyRowMapper<>(OrderHeaderDto.class);
            return jdbcTemplate.query(SQL, mapper, fechaInicio, fechaFin);
        } catch (Exception e) {
            throw new IllegalStateException("Ocurrio un error al consultar la base de datos", e);
        }
    }

    public List<OrderDetailDto> getAllOrderDetailsPendingUpdate(Map<String, List<String>> payload) {

        List<String> listaPedidos = payload.getOrDefault("pedidos", List.of());
        if (listaPedidos.isEmpty()) {
            return List.of(); // Si no hay pedidos, retornamos vacío sin ir a la BD
        }
        String SQL = """
                SELECT
                I_vDOCU_ORIGEN as docOrigen,
                I_vID_TIPO     as idTipo,
                I_vITEMNMBR    as artCodigo,
                I_vLOCNCODE    as bodega,
                DEX_ROW_ID     as dexRowId
                FROM
                MSW_TB_FACTURA_DET
                WHERE DESCRIPCION_ORG IS NULL
                AND VALOR_ORG IS NULL
                AND DESCUENTO_ORG IS NULL
                AND I_vDOCU_ORIGEN IN (:listaPedidos)
                ORDER BY DEX_ROW_ID
                """;
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            // 3. Pasamos SOLO la lista, no el mapa completo
            parameters.addValue("listaPedidos", listaPedidos);

            var mapper = new BeanPropertyRowMapper<>(OrderDetailDto.class);
            return namedParameterJdbcTemplate.query(SQL, parameters, mapper);
        } catch (Exception e) {
            throw new IllegalStateException("Ocurrio un error al consultar la base de datos", e);
        }
    }

    // actualizacion masiva de datos
    @Transactional
    public int[] updateMassiveHeaders(List<OrdersHeadersRequest> headers) {
        String sql = """
                UPDATE MSW_TB_FACTURA_CAB
                SET USERID = ?,
                    COND_PAGO_ORG = ?
                WHERE DEX_ROW_ID = ?
                """;

        return jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                OrdersHeadersRequest header = headers.get(i);
                // Orden de los ? en el SQL
                ps.setString(1, header.getUserIdCreator());
                ps.setString(2, header.getCondIdOriginal());
                ps.setLong(3, header.getDexRowId()); 
            }

            @Override
            public int getBatchSize() {
                return headers.size();
            }
        });
    }

    @Transactional
    public int[] updateMassiveDetails(List<OrdersDetailRequest> details) {
      
        String sql = """
                UPDATE MSW_TB_FACTURA_DET 
                SET
                DESCRIPCION_ORG = ?,
                VALOR_ORG = ?, 
                DESCUENTO_ORG = ?
                WHERE DEX_ROW_ID = ?
                """;

        return jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                OrdersDetailRequest detail = details.get(i);
                // Asegúrate de manejar BigDecimal correctamente
                ps.setString(1, detail.getDescripcionOrginal());  
                ps.setBigDecimal(2, detail.getValorOrginal());      
                ps.setBigDecimal(3, detail.getDescuentoOrginal());  
                ps.setLong(4, detail.getDexRowId());            // WHERE
            }

            @Override
            public int getBatchSize() {
                return details.size();
            }
        });
    }

}
