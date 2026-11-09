package tuval.api.web.reportes.resolucionarticulos.service.dynamics;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import tuval.api.web.reportes.resolucionarticulos.dao.dynamics.ResolucionArticulosReporteDao.Familia;

class ReglasArticuloTest {
  @ParameterizedTest
  @CsvSource({"½,1/2", "1½,3/2", "1 ½,3/2", "1 1/2,3/2", "1-1/2,3/2", "1.5,3/2", "¾,3/4", "1 / 2,1/2"})
  void normalizaFraccionesSinPerderEnteros(String medida, String expected) {
    assertThat(ReglasArticulo.interpretar("niple galvanizado " + medida + "\" por 2\"")
        .atributos().get("medidas")).isEqualTo(expected + " x 2");
  }

  @Test
  void conservaOrdenUnidadesYSeparadoresPegados() {
    var spec = ReglasArticulo.interpretar("NEPLO GALVANIZADO 1/2″X2″ NPT");
    assertThat(spec.familia()).isEqualTo(Familia.NIPLE);
    assertThat(spec.atributos()).containsEntry("medidas", "1/2 x 2")
        .containsEntry("unidadMedidas", "PULGADAS").containsEntry("rosca", "NPT");
    assertThat(ReglasArticulo.interpretar("niple 2 x 1/2 mm").atributos())
        .containsEntry("medidas", "2 x 1/2").containsEntry("unidadMedidas", "MM");
  }

  @Test
  void separaClaseAnguloCedulaYGradoDeMedidas() {
    assertThat(ReglasArticulo.interpretar("codo inoxidable 316 clase 300 sch40 1/2\" x90° NPT")
        .atributos()).containsEntry("medidas", "1/2").containsEntry("angulo", "90")
        .containsEntry("clase", "300").containsEntry("cedula", "40").containsEntry("gradoMaterial", "316");
  }

  @ParameterizedTest
  @ValueSource(strings = {"llave esférica 2", "válvula bola 2", "llave paso total 2"})
  void reconoceSinonimosAcotados(String text) {
    assertThat(ReglasArticulo.interpretar(text).familia()).isEqualTo(Familia.VALVULA_BOLA);
  }

  @ParameterizedTest
  @ValueSource(strings = {"codo galvanizado 45 90 1/2", "bushing bronce laton 1 x 1/2",
      "codo galvanizado 150 300 1/2 x90", "codo galvanizado NPT BSP 1/2 x90",
      "niple galvanizado 1/0 x2", "niple galvanizado 1/1000 x2", "bushing galvanizado 1.5.5",
      "bushing galvanizado -1 x 1/2", "bushing codo galvanizado 1 x 1/2",
      "bushing galvanizado 1 mm x 1/2 pulgadas"})
  void noDaPorComprendidosAtributosContradictoriosOFraccionesInvalidas(String text) {
    assertThat(ReglasArticulo.interpretar(text).pendientes()).isNotEmpty();
  }
}
