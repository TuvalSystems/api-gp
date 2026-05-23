package tuval.api.web.models.dao.tuval.otros;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import tuval.api.web.models.dto.otros.pedidos.OrderDetailDto;
import tuval.api.web.models.dto.otros.pedidos.OrderHeaderDto;
import tuval.api.web.models.request.otros.pedidos.OrderDetailListRequest;
import tuval.api.web.models.request.otros.pedidos.OrderDetailWebRequest;
import tuval.api.web.models.request.otros.pedidos.OrderListRequest;
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

    public List<OrderHeaderDto> getAllOrderPendingUpdate(OrderListRequest payload) {
        String query = """
                SELECT
                    I_vDOCU_ORIGEN AS docOrigen,
                    I_vID_TIPO AS idTipo,
                    I_vPUNTO_VENTA AS ptoVta,
                    DOC_GENERADO AS docGenerado,
                    DEX_ROW_ID AS dexRowId,
                    I_vLOCNCODE AS bodega
                FROM MSW_TB_FACTURA_CAB
                WHERE I_vDOCU_ORIGEN IN (:pedidos)
                ORDER BY DEX_ROW_ID
                """;

        try {
            if (payload == null || payload.getPedidos() == null || payload.getPedidos().isEmpty()) {
                return List.of();
            }

            var params = new MapSqlParameterSource();
            params.addValue("pedidos", payload.getPedidos());

            var mapper = new BeanPropertyRowMapper<>(OrderHeaderDto.class);

            return namedParameterJdbcTemplate.query(query, params, mapper);

        } catch (Exception e) {
            throw new IllegalStateException("Ocurrió un error al consultar la base de datos", e);
        }
    }

    public List<OrderDetailDto> getAllOrderDetailsPendingUpdate(OrderDetailListRequest payload) {

        if (payload == null || payload.getDetalles() == null || payload.getDetalles().isEmpty()) {
            return List.of();
        }

        List<OrderDetailWebRequest> detalles = payload.getDetalles()
                .stream()
                .filter(det -> det.getPedido() != null && !det.getPedido().isBlank())
                .filter(det -> det.getArtCodigo() != null && !det.getArtCodigo().isBlank())
                .filter(det -> det.getOrden() != null)
                .toList();

        if (detalles.isEmpty()) {
            return List.of();
        }
        StringBuilder condiciones = new StringBuilder();
        MapSqlParameterSource parameters = new MapSqlParameterSource();

        for (int i = 0; i < detalles.size(); i++) {
            OrderDetailWebRequest det = detalles.get(i);

            if (i > 0) {
                condiciones.append(" OR ");
            }

            condiciones.append("""
                        (
                            I_vDOCU_ORIGEN = :pedido%s
                            AND I_vITEMNMBR = :artCodigo%s
                            AND I_vLINEA = :orden%s
                        )
                    """.formatted(i, i, i));

            parameters.addValue("pedido" + i, det.getPedido());
            parameters.addValue("artCodigo" + i, det.getArtCodigo());
            parameters.addValue("orden" + i, det.getOrden());
        }
        String SQL = """
                SELECT
                    I_vDOCU_ORIGEN AS docOrigen,
                    I_vID_TIPO     AS idTipo,
                    I_vITEMNMBR    AS artCodigo,
                    I_vLOCNCODE    AS bodega,
                    I_vLINEA       AS linea,
                    DEX_ROW_ID     AS dexRowId
                FROM MSW_TB_FACTURA_DET
                WHERE  (
                      %s
                  )
                ORDER BY DEX_ROW_ID
                """.formatted(condiciones.toString());

        try {
            var mapper = new BeanPropertyRowMapper<>(OrderDetailDto.class);
            return namedParameterJdbcTemplate.query(SQL, parameters, mapper);
        } catch (Exception e) {
            throw new IllegalStateException("Ocurrió un error al consultar la base de datos", e);
        }
    }

    public int[] updateMassiveHeaders(List<OrdersHeadersRequest> headers) {

        String sql = """
                UPDATE MSW_TB_FACTURA_CAB
                SET PEDIDO_ID = ?,
                    APRO_COMER_USERID = ?
                WHERE DEX_ROW_ID = ?
                """;

        return jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                OrdersHeadersRequest header = headers.get(i);

                ps.setLong(1, header.getOrderId());

                if (header.getUserApprovalId() != null && !header.getUserApprovalId().isBlank()) {
                    ps.setString(2, header.getUserApprovalId());
                } else {
                    ps.setNull(2, Types.VARCHAR);
                }

                ps.setLong(3, header.getDexRowId());
            }

            @Override
            public int getBatchSize() {
                return headers.size();
            }
        });
    }

   public int[] updateMassiveDetails(List<OrdersDetailRequest> details) {

    String sql = """
            UPDATE MSW_TB_FACTURA_DET
            SET DETALLE_ID = ?,
                PEDIDO_ID = ?,
                DESCUENTO_FINAL = ?
            WHERE DEX_ROW_ID = ?
            """;

    return jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
        @Override
        public void setValues(PreparedStatement ps, int i) throws SQLException {
            OrdersDetailRequest detail = details.get(i);

            ps.setLong(1, detail.getPedidoDetalleId());
            ps.setLong(2, detail.getPedidoId());

            if (detail.getDescuento() != null) {
                ps.setBigDecimal(3, detail.getDescuento());
            } else {
                ps.setNull(3, Types.DECIMAL);
            }

            ps.setLong(4, detail.getDexRowId());
        }

        @Override
        public int getBatchSize() {
            return details.size();
        }
    });
}

}
