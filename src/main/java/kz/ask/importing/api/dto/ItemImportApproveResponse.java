package kz.ask.importing.api.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemImportApproveResponse {
    private UUID importId;
    private String status;
    private Integer productsCreated;
    private Integer offersCreated;
    private Integer rowsSkipped;
}
