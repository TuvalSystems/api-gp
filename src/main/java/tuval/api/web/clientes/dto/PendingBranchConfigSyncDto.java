package tuval.api.web.clientes.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PendingBranchConfigSyncDto {
    private Integer coEmp;
    private Integer coLoc;
    private String txCli;
    private Integer coVen;
    private LocalDateTime feUltmod;
    private Integer coUsrmod;
    private String stCligencom;
    private String stSolresantfec;
    private String stSolressinfec;
    private String stTipVen;
    private Integer stSinMySql;
}
