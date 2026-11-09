package tuval.api.web.reportes.cotizacionborrador;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tuval.api.web.reportes.cotizacionborrador.client.CotizacionBorradorClient;
import tuval.api.web.reportes.cotizacionborrador.controller.CotizacionBorradorReporteController;
import tuval.api.web.reportes.cotizacionborrador.dto.CotizacionBorradorReporteDto;
import tuval.api.web.reportes.cotizacionborrador.request.CotizacionBorradorReporteRequest;
import tuval.api.web.reportes.cotizacionborrador.service.tuvalbackend.*;

class CotizacionBorradorReporteHttpTest {
  private static final String URL = "/api/reportes/cotizacion-borrador";
  private static final String ID = "5af09cd3-c181-461f-8637-c468f02d9638";
  private static final String BODY = "{\"request_id\":\"" + ID
      + "\",\"cliente_ruc\":\"CLIENTE-PRUEBA\",\"pv_id\":7,\"items\":[{\"codigo\":\"ITEM-PRUEBA\"}]}";
  private final CotizacionBorradorClient client = mock(CotizacionBorradorClient.class);
  private MockMvc mvc;

  @BeforeEach void setup() {
    mvc = MockMvcBuilders.standaloneSetup(new CotizacionBorradorReporteController(
        new CotizacionBorradorReporteService(client))).build();
    when(client.crear(any(), anyString())).thenReturn(new CotizacionBorradorClient.Resultado(201,
        new CotizacionBorradorReporteDto(42L, "COT42", "borrador_app", ID, false,
            null, null, false, null, null, List.of(), List.of())));
  }

  @Test void creaYConservaOmisionesSinInventarDefaults() throws Exception {
    mvc.perform(post(URL).contentType("application/json").header("Authorization", "Bearer fixture-only")
        .content(BODY)).andExpect(status().isCreated()).andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.id").value(42)).andExpect(header().string("Cache-Control", "no-store"));
    var captor = org.mockito.ArgumentCaptor.forClass(CotizacionBorradorReporteRequest.class);
    verify(client).crear(captor.capture(), eq("Bearer fixture-only"));
    var json = new ObjectMapper().valueToTree(captor.getValue());
    assertThat(json.has("contextoValido")).isFalse();
    assertThat(json.has("modo_prueba")).isFalse();
    assertThat(json.has("user_id")).isFalse();
    assertThat(json.path("items").get(0).has("cantidad")).isFalse();
    assertThat(json.path("items").get(0).has("unidad")).isFalse();
    assertThat(json.path("request_id").asText()).isEqualTo(ID);
  }

  @Test void pilotoConservaActorDelBearerYSoloDelegaPropietarioSolicitado() throws Exception {
    mvc.perform(post(URL).contentType("application/json").header("Authorization", "Bearer fixture-only")
        .content("{\"request_id\":\"" + ID + "\",\"modo_prueba\":true,\"user_id\":30,\"items\":[{\"codigo\":\"ITEM-PRUEBA\"}]}"))
        .andExpect(status().isCreated());
    verify(client).crear(argThat(r -> r.userId() == 30 && r.clienteRuc() == null && r.pvId() == null), eq("Bearer fixture-only"));
  }

  @ParameterizedTest @ValueSource(strings = {"", "Basic abc", "Bearer ", "Bearer a b", "Bearer x\r\nX: y", "Bearer a,Bearer b"})
  void rechazaAutenticacionAusenteOMalformada(String header) throws Exception {
    var request = post(URL).contentType("application/json").content(BODY);
    if (!header.isEmpty()) request.header("Authorization", header);
    mvc.perform(request).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.success").value(false))
        .andExpect(header().string("WWW-Authenticate", "Bearer"));
    verifyNoInteractions(client);
  }

  @Test void rechazaCredencialesDuplicadasYDemasiadoLargas() throws Exception {
    mvc.perform(post(URL).contentType("application/json").content(BODY)
        .header("Authorization", "Bearer one", "Bearer two")).andExpect(status().isUnauthorized());
    mvc.perform(post(URL).contentType("application/json").content(BODY)
        .header("Authorization", "Bearer " + "x".repeat(4097))).andExpect(status().isUnauthorized());
    verifyNoInteractions(client);
  }

  @ParameterizedTest @ValueSource(strings = {"{}", "null", "{", "{\"items\":[]}",
      "{\"request_id\":null}", "{\"items\":[null]}"})
  void rechazaEstructuraInvalida(String body) throws Exception { invalid(body); }

  @ParameterizedTest @ValueSource(strings = {
      "\"request_id\":\"not-uuid\"", "\"request_id\":4", "\"cliente_ruc\":null", "\"cliente_ruc\":4",
      "\"cliente_ruc\":\" \"", "\"pv_id\":null", "\"pv_id\":0", "\"pv_id\":1.5", "\"pv_id\":\"7\"",
      "\"items\":null", "\"items\":[]", "\"items\":[null]", "\"modo_prueba\":null", "\"modo_prueba\":\"true\"",
      "\"contextoValido\":true", "\"user_id\":30", "\"user_id\":null", "\"precio\":10", "\"backend_url\":\"https://evil.invalid\""})
  void rechazaCamposInvalidosODesconocidos(String field) throws Exception {
    var mapper = new ObjectMapper();
    var body = (com.fasterxml.jackson.databind.node.ObjectNode) mapper.readTree(BODY);
    body.setAll((com.fasterxml.jackson.databind.node.ObjectNode) mapper.readTree("{" + field + "}"));
    invalid(body.toString());
  }

  @ParameterizedTest @ValueSource(strings = {"{}", "{\"codigo\":null}", "{\"codigo\":7}", "{\"codigo\":\" \"}",
      "{\"codigo\":\"ITEM\",\"cantidad\":null}", "{\"codigo\":\"ITEM\",\"cantidad\":0}",
      "{\"codigo\":\"ITEM\",\"cantidad\":-1}", "{\"codigo\":\"ITEM\",\"cantidad\":1000001}",
      "{\"codigo\":\"ITEM\",\"cantidad\":0.001}", "{\"codigo\":\"ITEM\",\"cantidad\":1e2}",
      "{\"codigo\":\"ITEM\",\"cantidad\":\"1e2\"}", "{\"codigo\":\"ITEM\",\"unidad\":null}",
      "{\"codigo\":\"ITEM\",\"precio\":4}", "{\"codigo\":\"ITEM\",\"estado\":\"aprobado\"}"})
  void rechazaItemsInvalidosSinLlamadaLaravel(String item) throws Exception {
    invalid(BODY.replace("{\"codigo\":\"ITEM-PRUEBA\"}", item));
  }

  @Test void limitaLoteLongitudesYDuplicados() throws Exception {
    invalid(BODY.replace("ITEM-PRUEBA", "x".repeat(31)));
    invalid(BODY.replace("CLIENTE-PRUEBA", "x".repeat(31)));
    invalid(BODY.replace("{\"codigo\":\"ITEM-PRUEBA\"}", "{\"codigo\":\"ITEM\"},{\"codigo\":\"ITEM\"}"));
    invalid(BODY.replace("{\"codigo\":\"ITEM-PRUEBA\"}", String.join(",", java.util.Collections.nCopies(21, "{\"codigo\":\"ITEM\"}"))));
  }

  @ParameterizedTest @ValueSource(ints = {401,403,409,422,429,502,503,504})
  void conservaEstadosSegurosSinDatosInternos(int status) throws Exception {
    when(client.crear(any(), anyString())).thenThrow(new CotizacionBorradorException(status));
    mvc.perform(post(URL).contentType("application/json").header("Authorization", "Bearer fixture-only")
        .content(BODY)).andExpect(status().is(status)).andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.data").doesNotExist());
  }

  @Test void erroresInesperadosNoFiltranSecretos() throws Exception {
    when(client.crear(any(), anyString())).thenThrow(new IllegalStateException("password=secret; SELECT; internal.host"));
    var body = mvc.perform(post(URL).contentType("application/json").header("Authorization", "Bearer fixture-only")
        .content(BODY)).andExpect(status().isInternalServerError()).andReturn().getResponse().getContentAsString();
    assertThat(body).doesNotContain("password", "secret", "SELECT", "internal.host", "fixture-only");
  }

  private void invalid(String body) throws Exception {
    mvc.perform(post(URL).contentType("application/json").header("Authorization", "Bearer fixture-only")
        .content(body)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false));
    verifyNoInteractions(client);
  }
}
