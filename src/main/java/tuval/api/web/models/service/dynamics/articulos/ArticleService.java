package tuval.api.web.models.service.dynamics.articulos;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tuval.api.web.models.dao.dynamics.articulos.ArticleDynamicsDao;
import tuval.api.web.models.dto.articulos.TravapItemSelectionResponse;
import tuval.api.web.models.request.bodegas.articulos.TravapItemSelectionRequest;

@Service
@RequiredArgsConstructor
public class ArticleService {

  private final ArticleDynamicsDao articleDao;

  public TravapItemSelectionResponse getTravapItemSelectionService(
      TravapItemSelectionRequest request) {
    return articleDao.getTravapItemSelection(request);
  }
}
