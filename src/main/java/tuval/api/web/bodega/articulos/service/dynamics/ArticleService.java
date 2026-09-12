package tuval.api.web.bodega.articulos.service.dynamics;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tuval.api.web.bodega.articulos.dao.dynamics.ArticleDynamicsDao;
import tuval.api.web.bodega.articulos.dto.ArticlePurchaseClassificationDto;
import tuval.api.web.bodega.articulos.dto.ArticleSegmentConfigurationDto;
import tuval.api.web.bodega.articulos.dto.TravapItemSelectionResponse;
import tuval.api.web.bodega.articulos.request.ArticleConfigSegmentRequest;
import tuval.api.web.bodega.articulos.request.TravapItemSelectionRequest;

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

  public ArticlePurchaseClassificationDto getArticlePurchaseClassification(String item) {
    return articleDao.getArticlePurchaseClassification(item);
  }
}
