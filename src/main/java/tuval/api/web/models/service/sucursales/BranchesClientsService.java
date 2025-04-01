package tuval.api.web.models.service.sucursales;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tuval.api.web.models.dao.sucursales.BranchesClientsDao;
import tuval.api.web.models.entity.sucursales.BranchesClientEntity;
import tuval.api.web.models.entity.sucursales.BranchesClientsResponseEntity;
import tuval.api.web.models.entity.sucursales.CoordinateListEntity;
import tuval.api.web.models.entity.sucursales.CoordinateListResponseEntity;

@Service
public class BranchesClientsService {

    @Autowired
    private BranchesClientsDao branchesClientsDao;

    public List<BranchesClientEntity> consultBranchesClients(String cliente, String fechacreacion,
            String fechamodificacion) {
        List<BranchesClientEntity> response = new ArrayList<>();

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            Date fCreacionDate = (fechacreacion == null || fechacreacion.isEmpty()) ? null : sdf.parse(fechacreacion);
            Date fModificacionDate = (fechamodificacion == null || fechamodificacion.isEmpty()) ? null
                    : sdf.parse(fechamodificacion);
            BranchesClientsResponseEntity responseEntity = branchesClientsDao.consultBranchClientList(cliente,
                    fCreacionDate != null ? new java.sql.Date(fCreacionDate.getTime()) : null,
                    fModificacionDate != null ? new java.sql.Date(fModificacionDate.getTime()) : null);

            if (responseEntity != null && responseEntity.getData() instanceof List) {
                Object data = responseEntity.getData();
                List<BranchesClientEntity> tempResponse = new ArrayList<>();
                if (data instanceof List<?>) {
                    for (Object item : (List<?>) data) {
                        if (item instanceof BranchesClientEntity) {
                            tempResponse.add((BranchesClientEntity) item);
                        }
                    }
                }
                response = tempResponse;
            }
        } catch (Exception e) {
            System.err.println("Error en servicio: " + e.getMessage());
        }
        return response;
    }


    public List<CoordinateListEntity> consultCoordinateList() {
        List<CoordinateListEntity> response = new ArrayList<>();
        try {
            CoordinateListResponseEntity responseEntity = branchesClientsDao.getCoordinateListSp();
            if (responseEntity != null && responseEntity.getData() instanceof List) {
                Object data = responseEntity.getData();
                List<CoordinateListEntity> tempResponse = new ArrayList<>();
                if (data instanceof List<?>) {
                    for (Object item : (List<?>) data) {
                        if (item instanceof CoordinateListEntity) {
                            tempResponse.add((CoordinateListEntity) item);
                        }
                    }
                }
                response = tempResponse;
            }
        } catch (Exception e) {
            System.err.println("Error en servicio: " + e.getMessage());
        }
        return response;
    }
}
