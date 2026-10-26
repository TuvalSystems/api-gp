package tuval.api.web.bodega.despachos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tuval.api.web.bodega.despachos.controller.ShippingGuideController;
import tuval.api.web.bodega.despachos.dao.dynamics.DispatchDeliveryNoteDao;
import tuval.api.web.bodega.despachos.request.GuidePurchaseOrderRequest;
import tuval.api.web.bodega.despachos.service.tuval.ShippingGuideService;

class GuidePurchaseOrderTest {
  private static final String URL = "/api/inventario/despachos/guia-remision-orden-compra";

  @Test
  void insertsWithBoundParametersAndPreservesNullableColumns() {
    JdbcTemplate jdbc = mock(JdbcTemplate.class);
    var dao = new DispatchDeliveryNoteDao(jdbc, new ObjectMapper());
    var request = new GuidePurchaseOrderRequest();
    request.setId(42);
    request.setIdGuiaRemision(10);
    request.setIdPedidoId(30);
    request.setTipoGuia("REMISION");
    request.setIdOc("OC'123");
    request.setFechaIngreso(LocalDateTime.of(2026, 10, 26, 10, 31, 44));

    dao.insertGuidePurchaseOrder(request);

    var sql = ArgumentCaptor.forClass(String.class);
    var args = ArgumentCaptor.forClass(Object[].class);
    var types = ArgumentCaptor.forClass(int[].class);
    verify(jdbc).update(sql.capture(), args.capture(), types.capture());
    assertThat(sql.getValue()).contains("[Dynamics2021].[dbo].[guia_remision_orden_compra]")
        .contains("VALUES (?, ?, ?, ?, ?, ?, ?)").doesNotContain("OC'123");
    assertThat(args.getValue()).containsExactly(42, 10, null, 30, "REMISION", "OC'123",
        Timestamp.valueOf("2026-10-26 10:31:44"));
    assertThat(types.getValue()).containsExactly(Types.INTEGER, Types.INTEGER, Types.INTEGER,
        Types.INTEGER, Types.VARCHAR, Types.VARCHAR, Types.TIMESTAMP);

    dao.insertGuidePurchaseOrder(new GuidePurchaseOrderRequest());
    verify(jdbc).update(anyString(), eq(new Object[7]), any(int[].class));
  }

  @Test
  void acceptsSqlDateFormatAndReturnsCreatedId() throws Exception {
    var service = mock(ShippingGuideService.class);
    mvc(service).perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("""
        {"id":42,"id_guia_remision":10,"id_guia_reserva":20,"id_pedido_id":30,
         "tipo_guia":"REMISION","id_oc":"OC123","fecha_ingreso":"2026-10-26 10:31:44"}
        """))
        .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.id").value(42));
    var request = ArgumentCaptor.forClass(GuidePurchaseOrderRequest.class);
    verify(service).insertGuidePurchaseOrder(request.capture());
    assertThat(request.getValue().getIdGuiaReserva()).isEqualTo(20);
    assertThat(request.getValue().getFechaIngreso()).isEqualTo("2026-10-26T10:31:44");
  }

  @Test
  void rejectsMissingIdAndOversizedOrderBeforeInserting() throws Exception {
    var service = mock(ShippingGuideService.class);
    MockMvc mvc = mvc(service);
    for (String json : new String[] {"{}", "{\"id\":42,\"id_oc\":\"123456789012345678\"}"}) {
      mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json))
          .andExpect(status().isBadRequest());
    }
    verifyNoInteractions(service);
  }

  @Test
  void duplicateIdUsesSharedErrorResponse() throws Exception {
    var service = mock(ShippingGuideService.class);
    doThrow(new DuplicateKeyException("duplicate")).when(service).insertGuidePurchaseOrder(any());
    mvc(service).perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{\"id\":42}"))
        .andExpect(status().isInternalServerError()).andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Ya existe un registro con el id 42"));
  }

  private MockMvc mvc(ShippingGuideService service) {
    return MockMvcBuilders.standaloneSetup(new ShippingGuideController(service)).build();
  }
}
