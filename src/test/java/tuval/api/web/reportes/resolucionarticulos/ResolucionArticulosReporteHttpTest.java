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
}
