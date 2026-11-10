package tuval.api.web.reportes.listadoitems;

import static org.assertj.core.api.Assertions.*;
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
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tuval.api.web.reportes.listadoitems.controller.ListadoItemsReporteController;
import tuval.api.web.reportes.listadoitems.dao.dynamics.ListadoItemsReporteDao;
import tuval.api.web.reportes.listadoitems.dto.ListadoItemsReporteDto;
import tuval.api.web.reportes.listadoitems.request.ListadoItemsReporteRequest;
import tuval.api.web.reportes.listadoitems.service.dynamics.ListadoItemsReporteService;

class ListadoItemsReporteHttpTest {
  private static final String URL = "/api/reportes/listado-items";
  private final ListadoItemsReporteDao dao = mock(ListadoItemsReporteDao.class);
  private final ObjectMapper mapper = new ObjectMapper();
  private MockMvc mvc;

  @BeforeEach
  void controller() {
    mvc = MockMvcBuilders.standaloneSetup(new ListadoItemsReporteController(new ListadoItemsReporteService(dao))).build();
    when(dao.consultar(any())).thenReturn(List.of());
  }

  @Test
  void defaultsReturnEmptySuccessList() throws Exception {
    var response = mvc.perform(get(URL)).andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith("application/json"))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Respuesta exitosa"))
        .andExpect(jsonPath("$.data").isArray()).andExpect(jsonPath("$.data").isEmpty())
        .andReturn().getResponse();
    assertThat(mapper.readTree(response.getContentAsString()).size()).isEqualTo(3);
    var request = capture();
    assertThat(request.getCodigo()).isNull();
    assertThat(request.getDescripcion()).isNull();
    assertThat(request.getMarca()).isNull();
    assertThat(request.getMaterial()).isNull();
    assertThat(request.getPage()).isZero();
    assertThat(request.getSize()).isEqualTo(100);
  }

  @Test
  void combinesAllFiltersAndReturnsExactlySevenOriginalJsonProperties() throws Exception {
    // Synthetic fixture; this does not validate SQL Server or real data.
    var dto = new ListadoItemsReporteDto("TEST-001", "ZAF", "3L", "TEST ITEM", "PZA", "TEST", null);
    when(dao.consultar(any())).thenReturn(List.of(dto));
    var response = mvc.perform(get(URL).param("codigo", "TEST-001").param("descripcion", "TEST ITEM")
        .param("marca", "TEST").param("material", "ACERO").param("page", "10000").param("size", "500"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data[0].tx_itm").value("TEST-001"))
        .andExpect(jsonPath("$.data[0].Descripcion").value("TEST ITEM"))
        .andReturn().getResponse();
    var row = mapper.readTree(response.getContentAsString()).path("data").get(0);
    assertThat(row.size()).isEqualTo(7);
    for (String column : ListadoItemsReporteDaoTest.COLUMNS) assertThat(row.has(column)).as(column).isTrue();
    assertThat(row.get("tx_atrMat").isNull()).isTrue();
    var request = capture();
    assertThat(request.getCodigo()).isEqualTo("TEST-001");
    assertThat(request.getDescripcion()).isEqualTo("TEST ITEM");
    assertThat(request.getMarca()).isEqualTo("TEST");
    assertThat(request.getMaterial()).isEqualTo("ACERO");
    assertThat(request.getPage()).isEqualTo(10000);
    assertThat(request.getSize()).isEqualTo(500);
  }

  static Stream<Arguments> invalidPaging() {
    return Stream.of(Arguments.of("page", "-1"), Arguments.of("page", "10001"),
        Arguments.of("page", "2147483648"), Arguments.of("page", "bad"), Arguments.of("page", ""),
        Arguments.of("size", "0"), Arguments.of("size", "-1"), Arguments.of("size", "501"),
        Arguments.of("size", "2147483648"), Arguments.of("size", "bad"), Arguments.of("size", ""));
  }

  @ParameterizedTest
  @MethodSource("invalidPaging")
  void rejectsInvalidPagingBeforeDao(String name, String value) throws Exception {
    assertInvalid(name, value);
  }

  static Stream<Arguments> fields() {
    return Stream.of(Arguments.of("codigo", 31), Arguments.of("descripcion", 101),
        Arguments.of("marca", 100), Arguments.of("material", 100));
  }

  @ParameterizedTest
  @MethodSource("fields")
  void rejectsBlankAndOverlengthFiltersBeforeDao(String name, int limit) throws Exception {
    for (String value : new String[] {"", "   ", "\t\n", "x".repeat(limit + 1)}) assertInvalid(name, value);
  }

  @ParameterizedTest
  @MethodSource("fields")
  void acceptsEachFilterAtItsMaximumLength(String name, int limit) throws Exception {
    mvc.perform(get(URL).param(name, "x".repeat(limit))).andExpect(status().isOk());
    verify(dao).consultar(any());
  }

  @Test
  void sqlErrorsAreGenericAndDoNotExposeDetails() throws Exception {
    when(dao.consultar(any())).thenThrow(new DataAccessResourceFailureException(
        "SELECT tx_itm FROM dbo.vw_listadoItems; server=private-host; password=secret"));
    var response = mvc.perform(get(URL)).andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("No se pudo consultar el listado de items"))
        .andExpect(jsonPath("$.data").doesNotExist()).andReturn().getResponse();
    assertThat(response.getContentAsString()).doesNotContain("SELECT", "private-host", "secret", "dbo.");
  }

  @Test
  void illegalArgumentErrorsAlsoUseGeneric400() throws Exception {
    when(dao.consultar(any())).thenThrow(new IllegalArgumentException("private-host secret SELECT"));
    var response = mvc.perform(get(URL)).andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Parametros invalidos del listado de items"))
        .andReturn().getResponse();
    assertThat(response.getContentAsString()).doesNotContain("private-host", "secret", "SELECT");
  }

  private void assertInvalid(String name, String value) throws Exception {
    var response = mvc.perform(get(URL).param(name, value)).andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Parametros invalidos del listado de items"))
        .andExpect(jsonPath("$.data").doesNotExist()).andReturn().getResponse();
    assertThat(mapper.readTree(response.getContentAsString()).size()).isEqualTo(2);
    verifyNoInteractions(dao);
  }

  private ListadoItemsReporteRequest capture() {
    var request = ArgumentCaptor.forClass(ListadoItemsReporteRequest.class);
    verify(dao).consultar(request.capture());
    return request.getValue();
  }
}
