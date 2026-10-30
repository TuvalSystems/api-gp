package tuval.api.web.reportes.articulos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import tuval.api.web.reportes.articulos.dao.dynamics.ArticulosReporteDao;
import tuval.api.web.reportes.articulos.request.ArticulosReporteRequest;
import tuval.api.web.reportes.articulos.service.dynamics.ArticulosReporteService;

class ArticulosReporteServiceTest {
  @Test
  void directCallersCannotBypassPageAndMaximumSizeRules() {
    var dao = mock(ArticulosReporteDao.class);
    var service = new ArticulosReporteService(dao);
    assertThatThrownBy(() -> service.consultar(null)).isInstanceOf(IllegalArgumentException.class);
    for (Integer page : new Integer[] {null, -1}) {
      var request = new ArticulosReporteRequest();
      request.setPage(page);
      assertThatThrownBy(() -> service.consultar(request)).isInstanceOf(IllegalArgumentException.class);
    }
    for (Integer size : new Integer[] {null, -1, 0, 501}) {
      var request = new ArticulosReporteRequest();
      request.setSize(size);
      assertThatThrownBy(() -> service.consultar(request)).isInstanceOf(IllegalArgumentException.class);
    }
    var request = new ArticulosReporteRequest();
    request.setSort(null);
    assertThatThrownBy(() -> service.consultar(request)).isInstanceOf(IllegalArgumentException.class);
    request.setSort(ArticulosReporteRequest.Sort.tx_itm);
    request.setDirection(null);
    assertThatThrownBy(() -> service.consultar(request)).isInstanceOf(IllegalArgumentException.class);
    verifyNoInteractions(dao);
  }

  @Test
  void emptyResultsRemainAnEmptyList() {
    var dao = mock(ArticulosReporteDao.class);
    var request = new ArticulosReporteRequest();
    when(dao.consultar(request)).thenReturn(List.of());
    assertThat(new ArticulosReporteService(dao).consultar(request)).isEmpty();
    verify(dao).consultar(request);
  }
}
