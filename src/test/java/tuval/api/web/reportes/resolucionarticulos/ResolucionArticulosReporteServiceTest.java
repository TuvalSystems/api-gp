package tuval.api.web.reportes.resolucionarticulos;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.dao.QueryTimeoutException;
import tuval.api.web.reportes.resolucionarticulos.dao.dynamics.ResolucionArticulosReporteDao;
import tuval.api.web.reportes.resolucionarticulos.dao.dynamics.ResolucionArticulosReporteDao.Familia;
import tuval.api.web.reportes.resolucionarticulos.dto.*;
import tuval.api.web.reportes.resolucionarticulos.request.ResolucionArticulosReporteRequest;
import tuval.api.web.reportes.resolucionarticulos.request.ResolucionArticulosReporteRequest.Renglon;
import tuval.api.web.reportes.resolucionarticulos.service.dynamics.ResolucionArticulosReporteService;

class ResolucionArticulosReporteServiceTest {
  private final ResolucionArticulosReporteDao dao = mock(ResolucionArticulosReporteDao.class);
  private final ResolucionArticulosReporteService service = new ResolucionArticulosReporteService(dao);

  @BeforeEach
  void fuenteVacia() { when(dao.buscar(any(), any())).thenReturn(List.of()); }

  // Codes supplied by the user; descriptions are fixtures, with verified DEV wording for 1 x 1/2.
  @ParameterizedTest
  @CsvSource({"9,1,1/2,020-000006-I,022-000084-I", "2,2,1,020-000017-I,022-000092-I",
      "6,2,1/2,020-000015-I,022-000090-I"})
  void bushingConservaClase150Y300(int cantidad, String a, String b, String gp150, String gp300) {
    when(dao.buscar(Familia.BUSHING, null)).thenReturn(List.of(
        row(gp150, "MECH BUSHING GALVANIZADO 150 UL/FM " + a + "\" X " + b + "\" NPT"),
        row(gp300, "MECH BUSHING GALVANIZADO 300 UL/FM " + a + "\" X " + b + "\" NPT")));
    var result = consultar("bushing galvanizado " + a + " por " + b, new BigDecimal(cantidad));
    assertThat(result.estado()).isEqualTo("ambiguo");
    assertThat(result.candidatos()).extracting(c -> c.codigoGP()).containsExactly(gp150, gp300);
    assertThat(result.diferencias()).containsEntry("clase", List.of("150", "300"));
    assertThat(result.cantidad()).isEqualTo(new BigDecimal(cantidad));
    assertThat(result.cantidadAsumida()).isFalse();
    assertThat(result.articulo()).isNull();
  }

  @ParameterizedTest
  @CsvSource({"1,90,020-000068-I,020-000069-I,022-000115-I", "1/2,90,020-000064-I,020-000065-I,022-000113-I",
      "1/2,45,020-000046-I,020-000047-I,022-000102-I"})
  void codosConservanTresVariantes(String medida, String angulo, String a, String b, String c) {
    when(dao.buscar(Familia.CODO, null)).thenReturn(List.of(
        row(a, "MECH CODO " + angulo + " GALVANIZADO 150 " + medida + "\" NPT"),
        row(b, "MECH CODO " + angulo + " GALVANIZADO 150 REFORZADO " + medida + "\" NPT"),
        row(c, "MECH CODO " + angulo + " GALVANIZADO 300 " + medida + "\" NPT")));
    var result = consultar("codo galvanizado " + medida + " por " + angulo, null);
    assertThat(result.estado()).isEqualTo("ambiguo");
    assertThat(result.candidatos()).extracting(x -> x.codigoGP()).containsExactly(a, b, c);
  }

  @ParameterizedTest
  @CsvSource({"2,037-000014-S,037-000014-I", "4,037-000017-S,037-000016-I"})
  void neplosConservanRefuerzoYLongitud(String largo, String a, String b) {
    when(dao.buscar(Familia.NIPLE, null)).thenReturn(List.of(
        row(a, "SM NEPLO GALVANIZADO 1/2\" X " + largo + "\" NPT"),
        row(b, "SM NEPLO GALVANIZADO SIN COSTURA REFORZADO 1/2\" X " + largo + "\""),
        row("otro", "SM NEPLO GALVANIZADO 1/2\" X 8\" NPT")));
    var result = consultar("niple galvanizado ½ por " + largo, null);
    assertThat(result.estado()).isEqualTo("ambiguo");
    assertThat(result.candidatos()).extracting(x -> x.codigoGP()).containsExactly(a, b);
  }

  @Test
  void nudoEsAmbiguoEntreTresClases() {
    when(dao.buscar(Familia.NUDO, null)).thenReturn(List.of(
        row("020-000103-I", "NUDO GALVANIZADO 150 2\" NPT"),
        row("020-000104-I", "NUDO GALVANIZADO 150 REFORZADO 2\" NPT"),
        row("022-000128-I", "NUDO GALVANIZADO 300 2\" NPT")));
    assertThat(consultar("nudo 2", null).candidatos()).hasSize(3);
  }

  @ParameterizedTest
  @ValueSource(strings = {"2", "1/2"})
  void llavesNoEligenMaterialOMarca(String medida) {
    when(dao.buscar(Familia.VALVULA_BOLA, null)).thenReturn(List.of(
        row("A", "LLAVE PASO TOTAL BRONCE " + medida + "\" NPT"),
        row("B", "VALVULA BOLA LATON " + medida + "\" NPT"),
        row("C", "VALVULA BOLA INOXIDABLE " + medida + "\" NPT")));
    var result = consultar("llave paso total " + medida, null);
    assertThat(result.estado()).isEqualTo("ambiguo");
    assertThat(result.candidatos()).hasSize(3);
  }

  @Test
  void teflonGrandeNoDefineTresCuartosOUnaPulgada() {
    when(dao.buscar(Familia.TEFLON, null)).thenReturn(List.of(
        row("A", "CINTA TEFLON 3/4\""), row("B", "CINTA TEFLON 1\"")));
    var result = consultar("teflon grande", null);
    assertThat(result.estado()).isEqualTo("ambiguo");
    assertThat(result.candidatos()).hasSize(2);
    assertThat(result.datosFaltantes()).contains("ancho_largo_espesor_teflon", "termino:GRANDE");
  }

  @Test
  void identificaSoloConBusquedaCompletaYEspecificacionComprendida() {
    bushingUnico();
    var result = consultar("MECH bushing galvanizado 150 1\" por ½\" NPT", null);
    assertThat(result.estado()).isEqualTo("identificado");
    assertThat(result.articulo().codigoGP()).isEqualTo("020-000006-I");
    assertThat(result.cantidadAsumida()).isTrue();
    assertThat(result.cantidad()).isEqualByComparingTo("1");
    assertThat(result.datosFaltantes()).isEmpty();
  }

  @Test
  void unaSolaFilaNoBastaSiQuedanTerminosOMaterialDesconocidos() {
    bushingUnico();
    var result = consultar("bushing galvanizado 150 1\" por 1/2\" NPT certificado", null);
    assertThat(result.estado()).isEqualTo("ambiguo");
    assertThat(result.datosFaltantes()).contains("termino:CERTIFICADO");
  }

  @ParameterizedTest
  @ValueSource(strings = {"bushing codo galvanizado 150 1\" x 1/2\" NPT",
      "bushing galvanizado 150 1\" x 1/2\" sin NPT", "bushing galvanizado 150 1\" + 1/2\" NPT"})
  void noDescartaTiposNegacionesNiSeparadoresNoComprendidos(String text) {
    bushingUnico();
    assertThat(consultar(text, null).estado()).isEqualTo("ambiguo");
  }

  @Test
  void atributosContradictoriosEnCatalogoImpidenIdentificar() {
    when(dao.buscar(Familia.BUSHING, null)).thenReturn(List.of(
        row("020-000006-I", "BUSHING GALVANIZADO 150 300 1\" X 1/2\" NPT")));
    var result = consultar("bushing galvanizado 150 1\" x 1/2\" NPT", null);
    assertThat(result.estado()).isEqualTo("ambiguo");
    assertThat(result.datosFaltantes()).contains("catalogo:clases_multiples");
  }

  @Test
  void noDescartaUnaFilaInciertaParaFabricarUnaCoincidenciaUnica() {
    when(dao.buscar(Familia.BUSHING, null)).thenReturn(List.of(
        row("incierto", "BUSHING GALVANIZADO 150 300 1\" X 1/2\" NPT"),
        row("conocido", "BUSHING GALVANIZADO 300 1\" X 1/2\" NPT")));
    var result = consultar("bushing galvanizado 300 1\" x 1/2\" NPT", null);
    assertThat(result.estado()).isEqualTo("ambiguo");
    assertThat(result.candidatos()).hasSize(2);
    when(dao.buscar(Familia.VALVULA_BOLA, null)).thenReturn(List.of(
        row("incierto", "VALVULA BRONCE 2\" NPT"),
        row("bola", "VALVULA BOLA BRONCE 2\" NPT")));
    assertThat(consultar("MECH valvula bola bronce 2\" NPT", null).estado()).isEqualTo("ambiguo");
  }

  @Test
  void noConfundeMediaConUnaYMediaNiInvierteMedidas() {
    when(dao.buscar(Familia.NIPLE, null)).thenReturn(List.of(
        row("media", "NIPLE GALVANIZADO 1/2\" X 2\""),
        row("mixta", "NIPLE GALVANIZADO 1 1/2\" X 2\""),
        row("invertida", "NIPLE GALVANIZADO 2\" X 1/2\"")));
    assertThat(consultar("neplo galvanizado 1½ por2", null).candidatos())
        .extracting(x -> x.codigoGP()).containsExactly("mixta");
  }

  @Test
  void distingueVacioCompletoDeTimeoutYVocabularioNoSoportado() {
    assertThat(consultar("codo galvanizado 1 por90", null).estado()).isEqualTo("sin_coincidencia");
    when(dao.buscar(Familia.CODO, null)).thenThrow(new QueryTimeoutException("interno"));
    var timeout = consultar("codo galvanizado 1 por90", null);
    assertThat(timeout.estado()).isEqualTo("ambiguo");
    assertThat(timeout.busquedaCompleta()).isFalse();
    assertThat(timeout.observaciones()).contains("timeout_de_busqueda");
    assertThat(consultar("algo desconocido", null).estado()).isEqualTo("ambiguo");
  }

  @Test
  void exploracionTruncadaNuncaProduceIdentificadoNiSinCoincidencia() {
    var rows = new ArrayList<CatalogoArticuloDto>();
    rows.add(row("020-000006-I", "BUSHING GALVANIZADO 150 1\" X 1/2\" NPT"));
    for (int i = 0; i < 500; i++) rows.add(row("otro" + i, "BUSHING GALVANIZADO 300 2\" X 1\" NPT"));
    when(dao.buscar(Familia.BUSHING, null)).thenReturn(rows);
    var result = consultar("bushing galvanizado 150 1\" por 1/2\" NPT", null);
    assertThat(result.candidatos()).hasSize(1);
    assertThat(result.estado()).isEqualTo("ambiguo");
    assertThat(result.busquedaTruncada()).isTrue();
    assertThat(result.busquedaCompleta()).isFalse();
    assertThat(consultar("bushing 9 por8", null).estado()).isEqualTo("ambiguo");
  }

  @Test
  void limitarRespuestaAUnCandidatoNoEliminaAmbiguedad() {
    when(dao.buscar(Familia.BUSHING, null)).thenReturn(List.of(
        row("A", "BUSHING GALVANIZADO 150 1\" X 1/2\" NPT"),
        row("B", "BUSHING GALVANIZADO 300 1\" X 1/2\" NPT")));
    var result = service.consultar(new ResolucionArticulosReporteRequest(
        List.of(new Renglon("bushing 1 por1/2", null)), 1)).getFirst();
    assertThat(result.candidatos()).hasSize(1);
    assertThat(result.candidatosTruncados()).isTrue();
    assertThat(result.estado()).isEqualTo("ambiguo");
  }

  @Test
  void reusaFuenteSoloDuranteElLoteYAcotaBusquedasDistintas() {
    service.consultar(new ResolucionArticulosReporteRequest(List.of(
        new Renglon("bushing 1 por1/2", null), new Renglon("bushing 2 por1", null)), null));
    verify(dao, times(1)).buscar(Familia.BUSHING, null);
    var lines = new ArrayList<Renglon>();
    for (int i = 0; i < 10; i++) lines.add(new Renglon("codigo TEST" + i, null));
    var result = service.consultar(new ResolucionArticulosReporteRequest(lines, null));
    assertThat(result.get(6).observaciones()).contains("presupuesto_de_busqueda_agotado");
    assertThat(result.get(6).estado()).isEqualTo("ambiguo");
    verify(dao, times(7)).buscar(any(), any());
  }

  @Test
  void codigoExactoNoUsaSimilitudYDuplicadosPermanecenAmbiguos() {
    var row = row("020-000006-I", "BUSHING");
    when(dao.buscar(null, "020-000006-I")).thenReturn(List.of(row));
    assertThat(consultar("020-000006-I", null).estado()).isEqualTo("identificado");
    when(dao.buscar(null, "020-000006-I")).thenReturn(List.of(row, row));
    assertThat(consultar("020-000006-I", null).estado()).isEqualTo("ambiguo");
  }

  private void bushingUnico() {
    when(dao.buscar(Familia.BUSHING, null)).thenReturn(List.of(
        row("020-000006-I", "MECH BUSHING GALVANIZADO 150 UL/FM 1\" X 1/2\" NPT")));
  }
  private ResolucionArticulosReporteDto consultar(String text, BigDecimal qty) {
    return service.consultar(new ResolucionArticulosReporteRequest(List.of(new Renglon(text, qty)), null)).getFirst();
  }
  static CatalogoArticuloDto row(String gp, String description) {
    return new CatalogoArticuloDto(gp, null, null, description, "PZAS", "MECH", "NO_TIENE", null, null, null);
  }
}
