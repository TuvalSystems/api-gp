package tuval.api.web.clientes.sincronizacion.util;

import java.util.ArrayList;
import java.util.List;

import tuval.api.web.clientes.sincronizacion.dto.SyncClientAddressDetailDto;

public class SyncClientsBranchesUtils {

    public static List<SyncClientAddressDetailDto> processAddressStrings(String addressStr) {
    List<SyncClientAddressDetailDto> addresses = new ArrayList<>();
    
    if (addressStr == null || addressStr.trim().isEmpty()) {
        return addresses;
    }

    // Separamos primero por el asterisco para obtener cada bloque de dirección
    // El -1 en split es vital para no ignorar bloques vacíos si los hubiera
    String[] blocks = addressStr.split("\\*", -1);

    for (String block : blocks) {
        if (block.trim().isEmpty()) continue;

        // Separamos el bloque por el pipe |
        // Usamos -1 para que si el último campo (correo) está vacío, el array mantenga el tamaño
        String[] parts = block.split("\\|", -1);
        
        SyncClientAddressDetailDto address = new SyncClientAddressDetailDto();
        address.setIdDireccion(getPart(parts, 0));
        address.setDireccion(getPart(parts, 1));
        address.setContacto(getPart(parts, 2));
        address.setReferencia(getPart(parts, 3));
        address.setCiudad(getPart(parts, 4));
        address.setProvincia(getPart(parts, 5));
        address.setPais(getPart(parts, 6));
        address.setTerritorio(getPart(parts, 7));
        address.setVendedor(getPart(parts, 8));
        address.setTelefono(getPart(parts, 9));
        address.setCorreoSri(getPart(parts, 10));
        address.setCorreoSri(getPart(parts, 11));
        
        addresses.add(address);
    }
    return addresses;
}

    public static int countOccurrences(String str, String subStr) {

        return str.split(subStr, -1).length - 1;
    }

    private static String getPart(String[] parts, int index) {
        return (parts.length > index) ? parts[index].trim() : "";
    }
}
