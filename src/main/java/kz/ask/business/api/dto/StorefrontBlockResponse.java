package kz.ask.business.api.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StorefrontBlockResponse {
    private UUID blockId;
    private String blockType;
    private Integer displayOrder;
    private JsonNode config;
    private Boolean enabled;
}
