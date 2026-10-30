package tuval.api.web.reportes.articulos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tuval.api.web.reportes.articulos.controller.ArticulosReporteController;
import tuval.api.web.reportes.articulos.dao.dynamics.ArticulosReporteDao;
import tuval.api.web.reportes.articulos.dto.ArticulosReporteDto;
import tuval.api.web.reportes.articulos.request.ArticulosReporteRequest;
import tuval.api.web.reportes.articulos.service.dynamics.ArticulosReporteService;

class ArticulosReporteHttpTest {
  private static final String URL = "/api/reportes/articulos";
  private final ArticulosReporteDao dao = mock(ArticulosReporteDao.class);
  private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
  private MockMvc mvc;

  @BeforeEach
  void controller() {
    mvc = MockMvcBuilders.standaloneSetup(
        new ArticulosReporteController(new ArticulosReporteService(dao))).build();
    when(dao.consultar(any())).thenReturn(List.of());
  }

  @Test
  void noFiltersOrCredentialsUsesDefaultsAndEmptySuccessContract() throws Exception {
    var response = mvc.perform(get(URL)).andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith("application/json"))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Respuesta exitosa"))
        .andExpect(jsonPath("$.data").isEmpty()).andReturn().getResponse();
    assertThat(mapper.readTree(response.getContentAsString()).size()).isEqualTo(3);
    var request = capture();
    assertThat(request.getTx_itm()).isNull();
    assertThat(request.getPage()).isZero();
    assertThat(request.getSize()).isEqualTo(100);
    assertThat(request.getSort()).isEqualTo(ArticulosReporteRequest.Sort.tx_itm);
    assertThat(request.getDirection()).isEqualTo(ArticulosReporteRequest.Direction.ASC);
  }

  @ParameterizedTest
  @CsvSource({"tx_itm,966-000235-I", "codZafiro,966-152I", "descripcion,VALVULA"})
  void bindsIndividualFilters(String parameter, String value) throws Exception {
    mvc.perform(get(URL).param(parameter, value)).andExpect(status().isOk());
    var request = capture();
    assertThat(switch (parameter) {
      case "tx_itm" -> request.getTx_itm();
      case "codZafiro" -> request.getCodZafiro();
      default -> request.getDescripcion();
    }).isEqualTo(value);
  }

  @Test
  void combinesFiltersAndPagingAndReturnsAll37OriginalJsonNames() throws Exception {
    // Synthetic fixture, not a SQL Server integration result.
    var dto = mapper.readValue("""
        {"tx_itm":"TEST-001", "Descripcion":"Artículo de prueba", "precio":85.04380,
         "costo":48.78538, "peso":3.602500, "marUti":41.0000,
         "fechacreacion":"2025-01-23T12:34:56.997"}
        """, ArticulosReporteDto.class);
    when(dao.consultar(any())).thenReturn(List.of(dto));
    var response = mvc.perform(get(URL).param("tx_itm", "TEST-001").param("marca", "TEST")
        .param("material", "ACERO").param("page", "2").param("size", "500")
        .param("sort", "precio").param("direction", "DESC"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.data[0].tx_itm").value("TEST-001"))
        .andExpect(jsonPath("$.data[0].Descripcion").value("Artículo de prueba"))
        .andExpect(jsonPath("$.data[0].fechacreacion").value("2025-01-23T12:34:56.997"))
        .andReturn().getResponse();
    var json = mapper.readTree(response.getContentAsString(java.nio.charset.StandardCharsets.UTF_8));
    var row = json.path("data").get(0);
    assertThat(row.size()).isEqualTo(37);
    for (String column : ArticulosReporteDaoTest.COLUMNS) {
      assertThat(row.has(column)).as(column).isTrue();
    }
    assertThat(row.get("costo").decimalValue()).isEqualByComparingTo("48.78538");
    assertThat(row.get("transito").isNull()).isTrue();
    var request = capture();
    assertThat(request.getMarca()).isEqualTo("TEST");
    assertThat(request.getMaterial()).isEqualTo("ACERO");
    assertThat(request.getPage()).isEqualTo(2);
    assertThat(request.getSize()).isEqualTo(500);
    assertThat(request.getSort()).isEqualTo(ArticulosReporteRequest.Sort.precio);
    assertThat(request.getDirection()).isEqualTo(ArticulosReporteRequest.Direction.DESC);
  }

  static Stream<Arguments> invalidParameters() {
    return Stream.of(
        Arguments.of("page", "-1"), Arguments.of("page", "not-a-number"),
        Arguments.of("page", "2147483648"), Arguments.of("page", ""),
        Arguments.of("size", "-1"), Arguments.of("size", "0"), Arguments.of("size", "501"),
        Arguments.of("size", ""), Arguments.of("sort", "precio DESC; DELETE"),
        Arguments.of("sort", "unknown"), Arguments.of("sort", ""),
        Arguments.of("direction", "UP"), Arguments.of("direction", ""));
  }

  @ParameterizedTest
  @MethodSource("invalidParameters")
  void invalidPagingOrOrderReturnsShared400BeforeDao(String parameter, String value) throws Exception {
    assertInvalid(parameter, value);
  }

  static Stream<Arguments> lengths() {
    return Stream.of(Arguments.of("tx_itm", 31), Arguments.of("codZafiro", 15),
        Arguments.of("cod3L", 11), Arguments.of("descripcion", 101),
        Arguments.of("claseItem", 11), Arguments.of("unidadMedida", 9),
        Arguments.of("categoriaCompra", 200), Arguments.of("lineaCompra", 200),
        Arguments.of("claseCompra", 200), Arguments.of("marca", 200),
        Arguments.of("material", 200), Arguments.of("itemCabecera", 31),
        Arguments.of("catven", 3), Arguments.of("catpes", 3),
        Arguments.of("catfre", 3), Arguments.of("cattuv", 3));
  }

  @ParameterizedTest
  @MethodSource("lengths")
  void rejectsEachStringPastItsSqlLength(String parameter, int limit) throws Exception {
    assertInvalid(parameter, "x".repeat(limit + 1));
  }

  @ParameterizedTest
  @MethodSource("lengths")
  void acceptsEachStringAtItsSqlLength(String parameter, int limit) throws Exception {
    mvc.perform(get(URL).param(parameter, "x".repeat(limit))).andExpect(status().isOk());
    verify(dao).consultar(any());
  }

  @Test
  void sqlFailuresReturnGeneric500WithoutInternalDetails() throws Exception {
    when(dao.consultar(any())).thenThrow(new DataAccessResourceFailureException(
        "SELECT costo FROM dbo.vw_MaestroArticulo; server=private-host; password=secret"));
    var response = mvc.perform(get(URL)).andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("No se pudo consultar el reporte de artículos"))
        .andExpect(jsonPath("$.data").doesNotExist()).andReturn().getResponse();
    assertThat(response.getContentAsString()).doesNotContain("SELECT", "private-host", "secret", "costo");
  }

  private void assertInvalid(String parameter, String value) throws Exception {
    var response = mvc.perform(get(URL).param(parameter, value))
        .andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Parámetros inválidos del reporte de artículos"))
        .andExpect(jsonPath("$.data").doesNotExist()).andReturn().getResponse();
    assertThat(mapper.readTree(response.getContentAsString()).size()).isEqualTo(2);
    verifyNoInteractions(dao);
  }

  private ArticulosReporteRequest capture() {
    var request = ArgumentCaptor.forClass(ArticulosReporteRequest.class);
    verify(dao).consultar(request.capture());
    return request.getValue();
  }
}
