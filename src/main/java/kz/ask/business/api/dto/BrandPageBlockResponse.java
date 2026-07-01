package kz.ask.business.api.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BrandPageBlockResponse {
    private UUID id;
    private UUID businessId;
    private String blockType;
    private Integer displayOrder;
    private String configJson;
    private Boolean enabled;
}
