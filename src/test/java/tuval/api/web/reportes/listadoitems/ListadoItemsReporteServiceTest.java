package tuval.api.web.reportes.listadoitems;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tuval.api.web.reportes.listadoitems.dao.dynamics.ListadoItemsReporteDao;
import tuval.api.web.reportes.listadoitems.request.ListadoItemsReporteRequest;
import tuval.api.web.reportes.listadoitems.service.dynamics.ListadoItemsReporteService;

class ListadoItemsReporteServiceTest {
  private final ListadoItemsReporteDao dao = mock(ListadoItemsReporteDao.class);
  private final ListadoItemsReporteService service = new ListadoItemsReporteService(dao);

  @Test
  void rejectsNullAndInvalidPagingBeforeDao() {
    assertInvalid(null);
    for (Integer page : new Integer[] {null, -1, 10001, Integer.MAX_VALUE}) {
      var request = new ListadoItemsReporteRequest();
      request.setPage(page);
      assertInvalid(request);
    }
    for (Integer size : new Integer[] {null, -1, 0, 501, Integer.MAX_VALUE}) {
      var request = new ListadoItemsReporteRequest();
      request.setSize(size);
      assertInvalid(request);
    }
  }

  static Stream<Arguments> fields() {
    return Stream.of(
        Arguments.of((BiConsumer<ListadoItemsReporteRequest, String>) ListadoItemsReporteRequest::setCodigo, 31),
        Arguments.of((BiConsumer<ListadoItemsReporteRequest, String>) ListadoItemsReporteRequest::setDescripcion, 101),
        Arguments.of((BiConsumer<ListadoItemsReporteRequest, String>) ListadoItemsReporteRequest::setMarca, 100),
        Arguments.of((BiConsumer<ListadoItemsReporteRequest, String>) ListadoItemsReporteRequest::setMaterial, 100));
  }

  @ParameterizedTest
  @MethodSource("fields")
  void validatesEveryTextForDirectCallers(BiConsumer<ListadoItemsReporteRequest, String> setter, int limit) {
    for (String value : new String[] {"", "   ", "\t\r\n", "x".repeat(limit + 1)}) {
      var request = new ListadoItemsReporteRequest();
      setter.accept(request, value);
      assertInvalid(request);
    }
    var valid = new ListadoItemsReporteRequest();
    setter.accept(valid, "x".repeat(limit));
    when(dao.consultar(valid)).thenReturn(List.of());
    assertThat(service.consultar(valid)).isEmpty();
    verify(dao).consultar(valid);
  }

  @Test
  void optionalFiltersAndPagingBoundariesPassUnchanged() {
    for (int[] bounds : new int[][] {{0, 1}, {10000, 500}, {0, 100}}) {
      var request = new ListadoItemsReporteRequest();
      request.setPage(bounds[0]);
      request.setSize(bounds[1]);
      when(dao.consultar(request)).thenReturn(List.of());
      assertThat(service.consultar(request)).isEmpty();
      verify(dao).consultar(request);
    }
  }

  private void assertInvalid(ListadoItemsReporteRequest request) {
    assertThatThrownBy(() -> service.consultar(request)).isInstanceOf(IllegalArgumentException.class);
    verifyNoInteractions(dao);
  }
}
