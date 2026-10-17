package tuval.api.web.clientes.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor 
@AllArgsConstructor 
public class BranchConfigSyncKeyRequest {
    @NotNull
    private Integer coEmp;
    @NotNull
    private Integer coLoc;
    @NotBlank
    private String txCli;
}
