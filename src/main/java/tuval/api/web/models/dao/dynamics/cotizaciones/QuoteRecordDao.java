package tuval.api.web.models.dao.dynamics.cotizaciones;
import org.slf4j.Logger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import tuval.api.web.models.Utils.ResponseJsonUtils;
import tuval.api.web.models.dto.cotizaciones.ItemDescripcionAlternativaDto;
import tuval.api.web.models.entity.dynamics.cotizaciones.QuoteRecordEntity;

@Repository
public class QuoteRecordDao {

       private static final Logger log = LoggerFactory.getLogger(QuoteRecordDao.class);

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

    // @Autowired
    // private JdbcTemplate dynamicsJdbcTemplate;
    public void save(QuoteRecordEntity entity) {
        String sql = """
            INSERT INTO dbo.tbm_cabcotrec (
                tx_cot, tx_usr, tx_cliRuc, tx_cliRazSoc, tx_cotCliSri, tx_est,
                tx_bod, tx_ptoVta, tx_ptoVtaDesc, tx_cotReqapr, tx_cotAprCom,
                tx_cotMot, co_cotRec, fe_cotRec, tx_obsMot, fe_ing,
                tx_usrIng, fe_ultmod, tx_usrMod
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        jdbcTemplate.update(sql,
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
                entity.getTxUsrMod()
        );
    }

    /**
     * RETORNA LISTA DE ITEMS ALTERNATIVOS
     */
    public ResponseEntity<Map<String, Object>> findByItem(String item) {
        try {
            String sql = "SELECT * FROM vw_ListadoItemsDescripcionesAlternativas WHERE tx_itm = ?";
            List<ItemDescripcionAlternativaDto> itemsList = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ItemDescripcionAlternativaDto.class), item); // <ItemDescripcionAlternativaDto>
            if (itemsList.isEmpty()) {
                return ResponseJsonUtils.buildError("No se encontraron descripciones alternativas para el ítem: " + item);
            }

            return ResponseJsonUtils.buildSuccessWithData("Descripciones alternativas obtenidas correctamente.", itemsList); // buildSuccessWithData
            // return ResponseEntity.ok(response); 
        } catch (Exception e) {
           log.error("Error al consultar item {}: {}", item, e.getMessage(), e);
            return ResponseJsonUtils.buildException(e);
        }
    }

}
