package kz.ask.business.api.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StorefrontBlockRequest {
    private UUID blockId;
    private String blockType;
    private Integer displayOrder;
    private JsonNode config;
    private Boolean enabled;
}
