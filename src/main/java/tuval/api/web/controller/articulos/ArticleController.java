package tuval.api.web.controller.articulos;

import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tuval.api.web.models.Utils.ResponseJsonUtils;
import tuval.api.web.models.dto.articulos.TravapItemSelectionResponse;
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
}
