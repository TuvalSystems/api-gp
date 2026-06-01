package tuval.api.web.models.dto.cobros;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCollectionDto {
    private String docId;
    private LocalDate docDate;
    private LocalDate collectionApplyDate;
    private String collectionMonth;
    private String customerNumber;
    private String customerName;
    private String establishment;
    private String invoiceNumber;
    private BigDecimal invoiceAmount;
    private Integer typePayment;
    private String collectionId; // cobro
    private BigDecimal collectionAmount;
    private LocalDate collectionDocDate;
    private String customerPoNumber;
    private String numeroRecibo;
}
