package tuval.api.web.models.service.dynamics.articulos;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tuval.api.web.models.dao.dynamics.articulos.ArticleDynamicsDao;
import tuval.api.web.models.dto.articulos.ArticleSegmentConfigurationDto;
import tuval.api.web.models.dto.articulos.TravapItemSelectionResponse;
import tuval.api.web.models.request.bodegas.articulos.ArticleConfigSegmentRequest;
import tuval.api.web.models.request.bodegas.articulos.TravapItemSelectionRequest;

@Service
@RequiredArgsConstructor
public class ArticleService {

  private final ArticleDynamicsDao articleDao;

  public TravapItemSelectionResponse getTravapItemSelectionService(
      TravapItemSelectionRequest request) {
    return articleDao.getTravapItemSelection(request);
  }

  public List<ArticleSegmentConfigurationDto> getArticlesConfigurationSegmentService(
      ArticleConfigSegmentRequest req) {
    return articleDao.getArticlesSegmentConfiguration(req);
  }
}
