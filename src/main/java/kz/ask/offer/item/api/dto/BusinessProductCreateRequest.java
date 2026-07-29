package kz.ask.offer.item.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
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
public class BusinessProductCreateRequest {

    private UUID categoryId;
    private String categoryName;
    private UUID branchId;

    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 2000)
    private String description;

    @Size(max = 2048)
    private String deepLink;

    private List<String> tags;

    private Map<String, Object> attributes;

    @DecimalMin(value = "0", inclusive = true)
    private BigDecimal price;

    private Boolean isActive;
}
