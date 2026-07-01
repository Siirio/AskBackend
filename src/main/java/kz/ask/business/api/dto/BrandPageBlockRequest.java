package kz.ask.business.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BrandPageBlockRequest {
    private String blockType;
    private Integer displayOrder;
    private String configJson;
    private Boolean enabled;
}
