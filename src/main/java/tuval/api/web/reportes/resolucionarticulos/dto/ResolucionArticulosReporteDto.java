package tuval.api.web.reportes.resolucionarticulos.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ResolucionArticulosReporteDto(int indice, String texto, BigDecimal cantidad,
    boolean cantidadAsumida, String estado, Candidato articulo, List<Candidato> candidatos,
    List<String> datosFaltantes, Map<String, List<String>> diferencias,
    boolean busquedaCompleta, boolean busquedaTruncada, boolean candidatosTruncados,
    List<String> observaciones) {
  public record Candidato(String codigoGP, String codigoZafiro, String codigo3L,
      String descripcion, String unidad, Map<String, String> atributos) {}
}
