package tuval.api.web.models.service.tuval.otros;

import java.sql.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import tuval.api.web.models.dao.tuval.otros.GeneralDao;
import tuval.api.web.models.dto.otros.pedidos.OrderDetailDto;
import tuval.api.web.models.dto.otros.pedidos.OrderHeaderDto;
import tuval.api.web.models.request.otros.pedidos.OrdersDetailRequest;
import tuval.api.web.models.request.otros.pedidos.OrdersHeadersRequest;

@Service
@RequiredArgsConstructor
public class GeneralService {

    private final GeneralDao generalDao;

    public List<OrderHeaderDto> getAllOrderPendingUpdate(Date fechaInicio, Date fechaFin) {
        return generalDao.getAllOrderPendingUpdate(fechaInicio, fechaFin);
    }

    public List<OrderDetailDto> getAllOrderDetailsPendingUpdate(Map<String, List<String>> payload) {
        return generalDao.getAllOrderDetailsPendingUpdate(payload);
    }

    /** proceso de update masivo */
    @Transactional(rollbackFor = Exception.class)
    public int[] processMassiveHeaderUpdate(List<OrdersHeadersRequest> headers) {
        if (headers == null || headers.isEmpty()) {
            throw new IllegalArgumentException("La lista de cabeceras esta vacia");
        }

        // int[] rowsAffected = generalDao.updateMassiveHeaders(headers);
        // rowsAffected es un array con un 1 por cada fila actualizada exitosamente
        // return Map.of("updated", rowsAffected.length, "message", "Cabeceras actualizadas");
         return generalDao.updateMassiveHeaders(headers);
    }

    @Transactional(rollbackFor = Exception.class)
    public int[] processMassiveDetailUpdate(List<OrdersDetailRequest> details) {
        if (details == null || details.isEmpty()) {
            throw new IllegalArgumentException("La lista de DETALLES esta vacia");
        }

        // int[] rowsAffected = generalDao.updateMassiveDetails(details);
        return generalDao.updateMassiveDetails(details);
    }
}
