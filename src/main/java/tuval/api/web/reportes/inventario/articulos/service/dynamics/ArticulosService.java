package tuval.api.web.reportes.inventario.articulos.service.dynamics;

import java.util.List;
import tuval.api.web.reportes.inventario.articulos.dto.ArticulosDto;
import tuval.api.web.reportes.inventario.articulos.dto.ListadoItemsDto;
import tuval.api.web.reportes.inventario.articulos.dto.ResolucionArticulosDto;
import tuval.api.web.reportes.inventario.articulos.request.ArticulosRequest;
import tuval.api.web.reportes.inventario.articulos.request.ListadoItemsRequest;
import tuval.api.web.reportes.inventario.articulos.request.ResolucionArticulosRequest;

public interface ArticulosService {
  List<ArticulosDto> consultar(ArticulosRequest request);
  List<ListadoItemsDto> consultarListadoItems(ListadoItemsRequest request);
  List<ResolucionArticulosDto> resolverArticulos(ResolucionArticulosRequest request);
}
