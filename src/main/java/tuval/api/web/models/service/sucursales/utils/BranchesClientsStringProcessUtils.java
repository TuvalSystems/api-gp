package tuval.api.web.models.service.sucursales.utils;

import java.util.ArrayList;
import java.util.List;
import tuval.api.web.models.entity.sucursales.BranchesClientAddressEntity;


public class BranchesClientsStringProcessUtils {

    public static List<BranchesClientAddressEntity> processAddressStrings(String addressStr) {
        List<BranchesClientAddressEntity> addresses = new ArrayList<>();
        if (addressStr == null || addressStr.isEmpty()) {
            return addresses;
        }
        int numAddresses = countOccurrences(addressStr, "\\*");
        for (int i = 0; i < numAddresses; i++) {
            if (addressStr.indexOf("*") > 0) {
                String currentAddress = addressStr.substring(0, addressStr.indexOf("*"));
                String[] parts = currentAddress.split("\\|");

                BranchesClientAddressEntity address = new BranchesClientAddressEntity();
                address.setIdDireccion(getPart(parts, 0));
                address.setDireccion(getPart(parts, 1));
                address.setProvincia(getPart(parts, 2));
                address.setCiudad(getPart(parts, 3));
                address.setPais(getPart(parts, 4));
                address.setTerritorio(getPart(parts, 5));
                address.setReferencia(getPart(parts, 6));
                address.setVendedor(getPart(parts, 7));
                address.setContacto(getPart(parts, 8));
                address.setTelefono(getPart(parts, 9));
                address.setCorreo_sri(getPart(parts, 10));
                addresses.add(address);
                addressStr = addressStr.substring(addressStr.indexOf("*") + 1);
            }
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
