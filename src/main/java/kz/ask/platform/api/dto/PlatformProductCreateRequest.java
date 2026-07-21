package kz.ask.platform.api.dto;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PlatformProductCreateRequest {

    private String name;
    private String categoryLabel;
    private String description;
    private String sku;
    private List<String> tags;
    private Map<String, String> characteristics;
}
