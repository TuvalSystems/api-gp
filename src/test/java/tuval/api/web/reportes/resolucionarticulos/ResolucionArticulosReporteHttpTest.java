package tuval.api.web.reportes.resolucionarticulos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tuval.api.web.reportes.resolucionarticulos.controller.ResolucionArticulosReporteController;
import tuval.api.web.reportes.resolucionarticulos.dao.dynamics.ResolucionArticulosReporteDao;
import tuval.api.web.reportes.resolucionarticulos.service.dynamics.ResolucionArticulosReporteService;

class ResolucionArticulosReporteHttpTest {
  private static final String URL = "/api/reportes/resolucion-articulos/consultar";
  private final ResolucionArticulosReporteDao dao = mock(ResolucionArticulosReporteDao.class);
  private MockMvc mvc;
  @BeforeEach
  void controller() {
    mvc = MockMvcBuilders.standaloneSetup(new ResolucionArticulosReporteController(
        new ResolucionArticulosReporteService(dao))).build();
    when(dao.buscar(any(), any())).thenReturn(List.of());
  }

  @Test
  void loteVacioDeResultadosConservaOrdenCantidadYContrato() throws Exception {
    mvc.perform(post(URL).contentType("application/json").content("""
        {"renglones":[{"texto":"codo 1 por90"},{"texto":"bushing 2 por1","cantidad":2.5}]}
        """))
        .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data[0].estado").value("sin_coincidencia"))
        .andExpect(jsonPath("$.data[0].indice").value(0))
        .andExpect(jsonPath("$.data[0].cantidad").value(1))
        .andExpect(jsonPath("$.data[0].cantidadAsumida").value(true))
        .andExpect(jsonPath("$.data[1].cantidad").value(2.5))
        .andExpect(jsonPath("$.data[1].cantidadAsumida").value(false));
  }

  @ParameterizedTest
  @ValueSource(strings = {"{}", "null", "{", "{\"renglones\":[]}",
      "{\"renglones\":[null]}", "{\"renglones\":[{}]}",
      "{\"renglones\":[{\"texto\":123}]}",
      "{\"renglones\":[{\"texto\":\" \"}]}",
      "{\"renglones\":[{\"texto\":\"codo\",\"cantidad\":0}]}",
      "{\"renglones\":[{\"texto\":\"codo\",\"cantidad\":-1}]}",
      "{\"renglones\":[{\"texto\":\"codo\",\"cantidad\":1000001}]}",
      "{\"renglones\":[{\"texto\":\"codo\",\"cantidad\":0.0001}]}",
      "{\"renglones\":[{\"texto\":\"codo\",\"cantidad\":\"mucho\"}]}",
      "{\"renglones\":[{\"texto\":\"codo\"}],\"maxCandidatos\":0}",
      "{\"renglones\":[{\"texto\":\"codo\"}],\"maxCandidatos\":1.5}",
      "{\"renglones\":[{\"texto\":\"codo\"}],\"maxCandidatos\":\"2\"}",
      "{\"renglones\":[{\"texto\":\"codo\"}],\"maxCandidatos\":6}"})
  void invalidoDevuelve400AntesDeDao(String json) throws Exception {
    mvc.perform(post(URL).contentType("application/json").content(json))
        .andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Parametros invalidos de resolucion de articulos"));
    verifyNoInteractions(dao);
  }

  @Test
  void limitaTextoYLoteAntesDeDao() throws Exception {
    invalidoDevuelve400AntesDeDao("{\"renglones\":[{\"texto\":\"" + "x".repeat(201) + "\"}]}");
    invalidoDevuelve400AntesDeDao("{\"renglones\":[" + String.join(",", java.util.Collections.nCopies(21,
        "{\"texto\":\"codo\"}")) + "]}");
  }

  @Test
  void candidatoSoloExponeListaPermitida() throws Exception {
    when(dao.buscar(any(), any())).thenReturn(List.of(ResolucionArticulosReporteServiceTest
        .row("020-000006-I", "BUSHING GALVANIZADO 150 1\" X 1/2\" NPT")));
    var response = mvc.perform(post(URL).contentType("application/json")
        .content("{\"renglones\":[{\"texto\":\"020-000006-I\"}]}"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.data[0].estado").value("identificado"))
        .andReturn().getResponse().getContentAsString();
    var candidate = new com.fasterxml.jackson.databind.ObjectMapper().readTree(response)
        .path("data").get(0).path("articulo");
    var names = new java.util.ArrayList<String>();
    candidate.fieldNames().forEachRemaining(names::add);
    assertThat(names).containsExactlyInAnyOrder("codigoGP", "codigoZafiro", "codigo3L", "descripcion", "unidad", "atributos");
    assertThat(candidate.path("unidad").isNull()).isTrue();
    assertThat(candidate.path("atributos").path("esquemaUnidad").asText()).isEqualTo("PZAS");
    assertThat(candidate.path("atributos").has("marca")).isFalse();
    assertThat(response).contains("unidad_comercial_no_disponible");
    assertThat(response).doesNotContain("precio", "costo", "marUti", "existencia");
  }

  @Test
  void erroresNoFiltranSqlServidorNiCredenciales() throws Exception {
    when(dao.buscar(any(), any())).thenThrow(new DataAccessResourceFailureException(
        "SELECT costo; server=interno; password=secret"));
    var response = mvc.perform(post(URL).contentType("application/json")
        .content("{\"renglones\":[{\"texto\":\"codo\"}]}"))
        .andExpect(status().isInternalServerError()).andExpect(jsonPath("$.success").value(false))
        .andReturn().getResponse().getContentAsString();
    assertThat(response).doesNotContain("SELECT", "costo", "interno", "secret");
  }

  @org.junit.jupiter.api.Nested
  class CreacionBorrador {
    private static final String CREATE = "/api/reportes/resolucion-articulos/crear-borrador";
    private static final String ID = "e0f7a456-2026-4000-8000-000000000001";
    private final com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
    private final ResolucionArticulosReporteService resolver = mock(ResolucionArticulosReporteService.class);
    private final tuval.api.web.reportes.resolucionarticulos.client.BorradorItemsPruebasClient backend =
        mock(tuval.api.web.reportes.resolucionarticulos.client.BorradorItemsPruebasClient.class);
    private MockMvc createMvc;

    @BeforeEach void crearController() {
      var service = new tuval.api.web.reportes.resolucionarticulos.service.tuvalbackend.ResolucionBorradorService(resolver, backend, mapper);
      createMvc = MockMvcBuilders.standaloneSetup(
          new tuval.api.web.reportes.resolucionarticulos.controller.ResolucionBorradorController(service)).build();
      when(resolver.consultar(any())).thenReturn(List.of(resolucion("identificado", true, List.of(candidato("020-000006-I")))));
      when(backend.crear(any())).thenReturn(new tuval.api.web.reportes.resolucionarticulos.dto.ResolucionBorradorDto(
          42L, "COT42", "borrador_app", null, null, null, List.of()));
    }

    private String json(String fields, String lines) {
      return "{\"request_id\":\"" + ID + "\",\"renglones\":" + lines + fields + "}";
    }
    private String normal() { return json("", "[{\"texto\":\"020-000006-I\"}]"); }
    private org.springframework.test.web.servlet.ResultActions enviar(String json) throws Exception {
      return createMvc.perform(post(CREATE).contentType("application/json").content(json));
    }
    private tuval.api.web.reportes.resolucionarticulos.dto.ResolucionArticulosReporteDto.Candidato candidato(String code) {
      return new tuval.api.web.reportes.resolucionarticulos.dto.ResolucionArticulosReporteDto.Candidato(
          code, "Z", "L", "articulo", null, java.util.Map.of());
    }
    private tuval.api.web.reportes.resolucionarticulos.dto.ResolucionArticulosReporteDto resolucion(
        String state, boolean complete, List<tuval.api.web.reportes.resolucionarticulos.dto.ResolucionArticulosReporteDto.Candidato> candidates) {
      return new tuval.api.web.reportes.resolucionarticulos.dto.ResolucionArticulosReporteDto(
          0, "texto", java.math.BigDecimal.ONE, true, state,
          state.equals("identificado") ? candidates.getFirst() : null, candidates, List.of(), java.util.Map.of(),
          complete, !complete, false, List.of("unidad_comercial_no_disponible"));
    }

    @Test void conectaResuelveUnaVezYEnviaSoloDatosRequeridosConDefaults() throws Exception {
      enviar(normal()).andExpect(status().isCreated()).andExpect(header().string("Cache-Control", "no-store"))
          .andExpect(jsonPath("$.data.borrador.numero").value("COT42"))
          .andExpect(jsonPath("$.data.resoluciones[0].cantidadAsumida").value(true));
      var captor = org.mockito.ArgumentCaptor.forClass(tuval.api.web.reportes.resolucionarticulos.client.BorradorItemsPruebasClient.Peticion.class);
      verify(backend).crear(captor.capture());
      assertThat(mapper.writeValueAsString(captor.getValue())).isEqualTo(
          "{\"cliente_ruc\":\"0990007020001\",\"pv_id\":2,\"items\":[{\"codigo\":\"020-000006-I\",\"cantidad\":1}]}");
      verify(resolver).consultar(any());
    }

    @Test void conservaClientePuntoVentaYCantidadExplicitos() throws Exception {
      var resolved = resolucion("identificado", true, List.of(candidato("020-000006-I")));
      when(resolver.consultar(any())).thenReturn(List.of(new tuval.api.web.reportes.resolucionarticulos.dto.ResolucionArticulosReporteDto(
          0, "texto", new java.math.BigDecimal("2.50"), false, resolved.estado(), resolved.articulo(), resolved.candidatos(),
          List.of(), java.util.Map.of(), true, false, false, List.of())));
      enviar(json(",\"cliente_ruc\":\"CLIENTE\",\"pv_id\":9", "[{\"texto\":\"020-000006-I\",\"cantidad\":2.50}]"))
          .andExpect(status().isCreated());
      var captor = org.mockito.ArgumentCaptor.forClass(tuval.api.web.reportes.resolucionarticulos.client.BorradorItemsPruebasClient.Peticion.class);
      verify(backend).crear(captor.capture());
      assertThat(captor.getValue().clienteRuc()).isEqualTo("CLIENTE");
      assertThat(captor.getValue().pvId()).isEqualTo(9);
      assertThat(captor.getValue().items().getFirst().cantidad()).isEqualByComparingTo("2.50");
    }

    @Test void noCreaAnteAmbiguosIncompletosOSinCoincidenciaNiCreaLotesParciales() throws Exception {
      for (var result : List.of(resolucion("ambiguo", true, List.of(candidato("020-000006-I"))),
          resolucion("identificado", false, List.of(candidato("020-000006-I"))),
          resolucion("sin_coincidencia", true, List.of()))) {
        when(resolver.consultar(any())).thenReturn(List.of(result));
        enviar(normal()).andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.data.creado").value(false));
      }
      when(resolver.consultar(any())).thenReturn(List.of(resolucion("identificado", true, List.of(candidato("020-000006-I"))),
          resolucion("ambiguo", false, List.of(candidato("020-000007-I")))));
      enviar(json("", "[{\"texto\":\"a\"},{\"texto\":\"b\"}]")).andExpect(status().isUnprocessableEntity());
      verify(backend, never()).crear(any());
    }

    @Test void seleccionExplicitaDebeSerUnCandidatoUnicoYNoEligeElPrimero() throws Exception {
      when(resolver.consultar(any())).thenReturn(List.of(resolucion("ambiguo", false,
          List.of(candidato("020-000006-I"), candidato("020-000007-I")))));
      enviar(json("", "[{\"texto\":\"bushing\",\"codigoSeleccionado\":\"020-000008-I\"}]"))
          .andExpect(status().isUnprocessableEntity());
      verify(backend, never()).crear(any());
      enviar(json("", "[{\"texto\":\"bushing\",\"codigoSeleccionado\":\"020-000007-I\"}]"))
          .andExpect(status().isCreated());
      var captor = org.mockito.ArgumentCaptor.forClass(tuval.api.web.reportes.resolucionarticulos.client.BorradorItemsPruebasClient.Peticion.class);
      verify(backend).crear(captor.capture());
      assertThat(captor.getValue().items().getFirst().codigo()).isEqualTo("020-000007-I");
    }

    @Test void noConsolidaCodigosRepetidosNiAceptaCandidatoDuplicado() throws Exception {
      var item = resolucion("identificado", true, List.of(candidato("020-000006-I")));
      when(resolver.consultar(any())).thenReturn(List.of(item, item));
      enviar(json("", "[{\"texto\":\"a\"},{\"texto\":\"b\"}]")).andExpect(status().isUnprocessableEntity());
      when(resolver.consultar(any())).thenReturn(List.of(resolucion("ambiguo", true,
          List.of(candidato("020-000006-I"), candidato("020-000006-I")))));
      enviar(json("", "[{\"texto\":\"bushing\",\"codigoSeleccionado\":\"020-000006-I\"}]"))
          .andExpect(status().isUnprocessableEntity());
      verify(backend, never()).crear(any());
    }

    @Test void mismoUuidReproduceSoloResultadoLocalYSuCambioEsConflicto() throws Exception {
      enviar(normal()).andExpect(status().isCreated());
      enviar(normal()).andExpect(status().isOk()).andExpect(jsonPath("$.data.repeticion_local").value(true));
      enviar(json(",\"pv_id\":3", "[{\"texto\":\"020-000006-I\"}]")).andExpect(status().isConflict());
      verify(backend, times(1)).crear(any()); verify(resolver, times(1)).consultar(any());
    }

    @Test void respuestaInciertaNoSeReintentaYNoFiltraDetalles() throws Exception {
      when(backend.crear(any())).thenThrow(new tuval.api.web.reportes.cotizacionborrador.service.tuvalbackend.CotizacionBorradorException(502));
      enviar(normal()).andExpect(status().isBadGateway()).andExpect(jsonPath("$.reintentable").value(false));
      enviar(normal()).andExpect(status().isConflict()).andExpect(jsonPath("$.reintentable").value(false));
      verify(backend, times(1)).crear(any());
    }

    @Test void segundoEnvioConcurrenteSeBloqueaAntesDeCrear() throws Exception {
      var started = new java.util.concurrent.CountDownLatch(1);
      var release = new java.util.concurrent.CountDownLatch(1);
      when(backend.crear(any())).thenAnswer(invocation -> {
        started.countDown(); release.await(5, java.util.concurrent.TimeUnit.SECONDS);
        throw new tuval.api.web.reportes.cotizacionborrador.service.tuvalbackend.CotizacionBorradorException(504);
      });
      try (var executor = java.util.concurrent.Executors.newSingleThreadExecutor()) {
        var first = executor.submit(() -> { try { return enviar(normal()).andReturn().getResponse().getStatus(); }
          catch (Exception exception) { throw new RuntimeException(exception); } });
        try {
          assertThat(started.await(5, java.util.concurrent.TimeUnit.SECONDS)).isTrue();
          enviar(normal()).andExpect(status().isConflict());
        } finally { release.countDown(); }
        assertThat(first.get(5, java.util.concurrent.TimeUnit.SECONDS)).isEqualTo(504);
      }
      verify(backend, times(1)).crear(any());
    }

    @Test void deshabilitadoNoConsultaCatalogoNiCrea() throws Exception {
      doThrow(new tuval.api.web.reportes.cotizacionborrador.service.tuvalbackend.CotizacionBorradorException(503))
          .when(backend).verificarConfiguracion();
      enviar(normal()).andExpect(status().isServiceUnavailable());
      verifyNoInteractions(resolver); verify(backend, never()).crear(any());
    }

    @ParameterizedTest @ValueSource(strings = {"{}", "null", "{", "{\"renglones\":[]}",
        "{\"texto\":\"a\",\"cantidad\":null}", "{\"texto\":\"a\",\"cantidad\":0}",
        "{\"texto\":\"a\",\"cantidad\":0.001}", "{\"texto\":\"a\",\"cantidad\":1e2}",
        "{\"texto\":\"a\",\"cantidad\":1000001}", "{\"texto\":\"a\",\"precio\":10}",
        "{\"texto\":123}", "{\"texto\":\" \"}", "{\"texto\":\"a\",\"codigoSeleccionado\":null}"})
    void rechazaDatosInvalidosSinConsultarNiCrear(String line) throws Exception {
      String body = line.equals("{}") || line.equals("null") || line.equals("{") || line.contains("renglones")
          ? line : json("", "[" + line + "]");
      enviar(body).andExpect(status().isBadRequest());
      verifyNoInteractions(resolver, backend);
    }

    @ParameterizedTest @ValueSource(strings = {",\"cliente_ruc\":null", ",\"cliente_ruc\":\" \"", ",\"pv_id\":0",
        ",\"pv_id\":\"2\"", ",\"pv_id\":null", ",\"user_id\":6", ",\"precio\":1", ",\"unidad\":\"UN\""})
    void rechazaContextoInvalidoOCamposAjenos(String fields) throws Exception {
      enviar(json(fields, "[{\"texto\":\"a\"}]")).andExpect(status().isBadRequest());
      verifyNoInteractions(resolver, backend);
    }
  }
}
