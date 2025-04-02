package tuval.api.web.controller.sucursales;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tuval.api.web.models.entity.tuval.sucursales.BranchesClientEntity;
import tuval.api.web.models.entity.tuval.sucursales.CoordinateListEntity;
import tuval.api.web.models.service.tuval.sucursales.BranchesClientsService;

@RestController
@RequestMapping("/api")
public class BranchesClientsController {

    @Autowired
    private BranchesClientsService branchesClientsService;

    @CrossOrigin
    @RequestMapping("/consultar-sucursales-clientes")
    public List<BranchesClientEntity> consultBranchesClients(@RequestParam(required = false) String cliente, 
    @RequestParam(required = false) String fechacreacion, @RequestParam(required = false) String fechamodificacion){ 
        try {
            List<BranchesClientEntity> response = branchesClientsService.consultBranchesClients(cliente, fechacreacion, fechamodificacion);
            if (response != null && response.size() > 0) {
                return response;
            }
            // Si no hay resultados, se devuelve una lista vac a
            return List.of();
        } catch (Exception e) {
            System.err.println("Error en servicio: " + e.getMessage());
            return List.of();
        }
    }


    @CrossOrigin
    @RequestMapping("/consultar-listado-coordenadas")
    public List<CoordinateListEntity> consultCoordinateListAll() {
        try {
            List<CoordinateListEntity> responseEntityList = branchesClientsService.consultCoordinateList();
            if (responseEntityList != null && !responseEntityList.isEmpty()) {
                return responseEntityList;
            }
            return List.of();
        } catch (Exception e) {
            System.err.println("Error en servicio: " + e.getMessage());
            return List.of();
        }
    }



}
