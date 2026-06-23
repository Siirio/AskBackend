package kz.ask.catalog.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    private UUID categoryId;

    @NotBlank
    private String name;

    private String description;

    private String sku;

    private List<String> tags;

    @DecimalMin(value = "0", inclusive = true)
    private BigDecimal price;

    private Boolean enabled;
}
