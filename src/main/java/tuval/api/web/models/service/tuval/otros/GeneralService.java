package tuval.api.web.models.service.tuval.otros;

import java.sql.Date;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import tuval.api.web.models.dao.dynamics.otros.GeneralDynamicsDao;
import tuval.api.web.models.dao.tuval.otros.GeneralDao;
import tuval.api.web.models.dto.otros.pedidos.OrderDetailDto;
import tuval.api.web.models.dto.otros.pedidos.OrderHeaderDto;
import tuval.api.web.models.request.otros.cotizaciones.QuoteHeaderRequest;
import tuval.api.web.models.request.otros.pedidos.OrderDetailListRequest;
import tuval.api.web.models.request.otros.pedidos.OrderListRequest;
import tuval.api.web.models.request.otros.pedidos.OrdersDetailListUpdateRequest;
import tuval.api.web.models.request.otros.pedidos.OrdersDetailRequest;
import tuval.api.web.models.request.otros.pedidos.OrdersHeadersListUpdateRequest;
import tuval.api.web.models.request.otros.pedidos.OrdersHeadersRequest;

@Service
@RequiredArgsConstructor
public class GeneralService {

    private final GeneralDao generalDao;
    private final GeneralDynamicsDao generalDynamicsDao;

    public List<OrderHeaderDto> getAllOrderPendingUpdate(OrderListRequest payload) {
        return generalDao.getAllOrderPendingUpdate(payload);
    }

    public List<OrderDetailDto> getAllOrderDetailsPendingUpdate(OrderDetailListRequest payload) {
        return generalDao.getAllOrderDetailsPendingUpdate(payload);
    }

    @Transactional(rollbackFor = Exception.class)
    public int[] processMassiveHeaderUpdate(OrdersHeadersListUpdateRequest payload) {

        if (payload == null || payload.getHeaders() == null || payload.getHeaders().isEmpty()) {
            throw new IllegalArgumentException("La lista de cabeceras está vacía");
        }

        List<OrdersHeadersRequest> headers = payload.getHeaders();

        validateHeaders(headers);

        int[] rowsAffected = generalDao.updateMassiveHeaders(headers);

        validateBatchResult("cabeceras", headers.size(), rowsAffected);

        return rowsAffected;
    }

    /**
     * Proceso de update masivo de detalles
     */
    @Transactional(rollbackFor = Exception.class)
    public int[] processMassiveDetailUpdate(OrdersDetailListUpdateRequest payload) {

        if (payload == null || payload.getDetalles() == null || payload.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La lista de detalles está vacía");
        }

        List<OrdersDetailRequest> details = payload.getDetalles();

        validateDetails(details);

        int[] rowsAffected = generalDao.updateMassiveDetails(details);

        validateBatchResult("detalles", details.size(), rowsAffected);

        return rowsAffected;
    }

    private void validateHeaders(List<OrdersHeadersRequest> headers) {
        for (int i = 0; i < headers.size(); i++) {
            OrdersHeadersRequest item = headers.get(i);

            if (item == null) {
                throw new IllegalArgumentException("La cabecera en la posición " + i + " es nula");
            }

            if (item.getDexRowId() == null) {
                throw new IllegalArgumentException("La cabecera en la posición " + i + " no tiene dexRowId");
            }

            if (item.getOrderId() == null) {
                throw new IllegalArgumentException("La cabecera en la posición " + i + " no tiene orderId");
            }
        }
    }

    private void validateDetails(List<OrdersDetailRequest> details) {
        for (int i = 0; i < details.size(); i++) {
            OrdersDetailRequest item = details.get(i);

            if (item == null) {
                throw new IllegalArgumentException("El detalle en la posición " + i + " es nulo");
            }

            if (item.getPedidoDetalleId() == null) {
                throw new IllegalArgumentException("El detalle en la posición " + i + " no tiene pedidoDetalleId");
            }

            if (item.getPedidoId() == null) {
                throw new IllegalArgumentException("El detalle en la posición " + i + " no tiene pedidoId");
            }

            if (item.getDexRowId() == null) {
                throw new IllegalArgumentException("El detalle en la posición " + i + " no tiene dexRowId");
            }
        }
    }

    private void validateBatchResult(String proceso, int expectedRows, int[] rowsAffected) {
        if (rowsAffected == null) {
            throw new IllegalStateException("El update masivo de " + proceso + " no devolvió resultado");
        }

        if (rowsAffected.length != expectedRows) {
            throw new IllegalStateException(
                    "El update masivo de " + proceso + " no procesó todos los registros. Esperados: "
                            + expectedRows + ", procesados: " + rowsAffected.length);
        }

        for (int i = 0; i < rowsAffected.length; i++) {
            int affected = rowsAffected[i];

            boolean success = affected > 0 ||
                    affected == Statement.SUCCESS_NO_INFO;

            if (!success) {
                throw new IllegalStateException(
                        "El update masivo de " + proceso + " falló en la posición " + i
                                + ". Filas afectadas: " + affected);
            }
        }
    }


    @Transactional (rollbackFor = Exception.class)
    public int[] processMassiveQuoteHeaderUpdate(List<QuoteHeaderRequest> payload)
    {
            if(payload == null || payload.isEmpty()){
                throw new IllegalArgumentException("La lista de cabeceras de cotización está vacía");
            }
            List<QuoteHeaderRequest> headers = payload;
            ValidateQuoteHeaders(headers);
            int[] rowsAffected = generalDynamicsDao.updateMassiveQuoteHeaders(headers);
            validateBatchResult("cabeceras de cotización", headers.size(), rowsAffected);
            return rowsAffected;
    }

    private void ValidateQuoteHeaders(List<QuoteHeaderRequest> headers) {
        for (int i = 0; i < headers.size(); i++) {
            QuoteHeaderRequest item = headers.get(i);

            if (item == null) {
                throw new IllegalArgumentException("La cabecera de cotización en la posición " + i + " es nula");
            }

            if (item.getCotizacion() == null) {
                throw new IllegalArgumentException("La cabecera de cotización en la posición " + i + " no tiene codigo");
            }

            
        }
    }

}
