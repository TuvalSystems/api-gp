package tuval.api.web.models.dao.dynamics.cliente;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import tuval.api.web.models.dto.cliente.SellerRequest;
import tuval.api.web.models.entity.dynamics.cliente.ListSellerEntity;

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
        String SQL = """
                SELECT co_usr as coUsr, tx_usr  as txUsr, co_tipven as coTipVen
                FROM dbo.vw_listadoVendedoresWeb
                WHERE tx_usr = ?
                """;
        try {
            var mapper = new org.springframework.jdbc.core.BeanPropertyRowMapper<>(ListSellerEntity.class);
            return jdbcTemplate.queryForObject(SQL, mapper, req.getUsuario());
        } catch (Exception e) {
            throw new IllegalStateException("Ocurrio un error al consultar la base de datos", e);
        }

    }

}
