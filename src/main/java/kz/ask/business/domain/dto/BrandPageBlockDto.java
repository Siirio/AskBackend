package kz.ask.business.domain.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BrandPageBlockDto {
    private UUID id;
    private UUID businessId;
    private String blockType;
    private Integer displayOrder;
    private String configJson;
    private Boolean enabled;
}
