package kz.ask.catalog.domain.dto;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private UUID id;
    private UUID businessId;
    private UUID categoryId;
    private String categoryLabel;
    private String name;
    private String description;
    private String sku;
    private List<String> tags;
    private Map<String, String> characteristics;
    private String status;
}
