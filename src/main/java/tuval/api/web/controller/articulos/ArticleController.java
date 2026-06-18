package tuval.api.web.controller.articulos;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tuval.api.web.models.Utils.ResponseJsonUtils;
import tuval.api.web.models.dto.articulos.ArticlePurchaseClassificationDto;
import tuval.api.web.models.dto.articulos.ArticleSegmentConfigurationDto;
import tuval.api.web.models.dto.articulos.TravapItemSelectionResponse;
import tuval.api.web.models.request.bodegas.articulos.ArticleConfigSegmentRequest;
import tuval.api.web.models.request.bodegas.articulos.TravapItemSelectionRequest;
import tuval.api.web.models.service.dynamics.articulos.ArticleService;

@RestController
@RequestMapping("api/articulos")
@RequiredArgsConstructor
public class ArticleController {

  private final ArticleService articleService;

  @PostMapping("seleccion-trampas-items")
  public ResponseEntity<Map<String, Object>> obtenerItemsTrampaVapor(
      @Valid @RequestBody TravapItemSelectionRequest payload) {
    try {
      TravapItemSelectionResponse response = articleService.getTravapItemSelectionService(payload);
      return ResponseJsonUtils.buildSuccessWithData("Items Obtendios", response);
    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }

  @CrossOrigin
  @PostMapping("/configuracion-segmento")
  public ResponseEntity<Map<String, Object>> obtenerConfiguracionSegmento(
      @RequestBody ArticleConfigSegmentRequest req) {
    try {
      List<ArticleSegmentConfigurationDto> response =
          articleService.getArticlesConfigurationSegmentService(req);
      return ResponseJsonUtils.buildSuccessWithData("Lista Obtenida Correctamente", response);
    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }

  @GetMapping("obtener-clasificacion")
  public ResponseEntity<Map<String, Object>> obtenerClasificacionArticulo(
      @Valid @RequestParam String item) {
    try {
      ArticlePurchaseClassificationDto response =
          articleService.getArticlePurchaseClassification(item);
      return ResponseJsonUtils.buildSuccessWithData(
          "Datos de Articulo Obtenido Correctamente", response);
    } catch (Exception e) {
      return ResponseJsonUtils.buildException(e);
    }
  }
}
