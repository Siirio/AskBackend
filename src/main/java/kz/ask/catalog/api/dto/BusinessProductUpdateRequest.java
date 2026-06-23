package kz.ask.catalog.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.util.List;
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
public class BusinessProductUpdateRequest {

    private UUID categoryId;

    private String name;

    private String description;

    private String sku;

    private List<String> tags;

    @DecimalMin(value = "0", inclusive = true)
    private BigDecimal price;

    private Boolean enabled;

    @JsonIgnore
    public boolean hasOnlyEnabledField() {
        return categoryId == null && name == null && description == null
                && sku == null && tags == null && price == null;
    }
}
