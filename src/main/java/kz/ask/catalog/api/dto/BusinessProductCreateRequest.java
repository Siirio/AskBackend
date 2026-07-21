package kz.ask.catalog.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class BusinessProductCreateRequest {

    private UUID categoryId;

    private String categoryLabel;

    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 2000)
    private String description;

    private String sku;

    private List<String> tags;

    @DecimalMin(value = "0", inclusive = true)
    private BigDecimal price;

    private Boolean enabled;

    private String imageUrl;
}
