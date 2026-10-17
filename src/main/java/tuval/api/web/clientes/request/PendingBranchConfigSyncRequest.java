package tuval.api.web.clientes.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PendingBranchConfigSyncRequest {
    @NotNull
    @Min(1)
    @Max(1000)
    private Integer limit = 1;

    @Valid
    private BranchConfigSyncKeyRequest cursor;
}
