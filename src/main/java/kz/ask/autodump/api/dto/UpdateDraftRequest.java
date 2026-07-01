package kz.ask.autodump.api.dto;

import java.math.BigDecimal;
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
public class UpdateDraftRequest {

    private String title;
    private String itemType;
    private String categoryLabel;
    private String description;
    private BigDecimal price;
    private String priceText;
    private String brand;
    private String tagsJson;
    private String customAttributesJson;
}
