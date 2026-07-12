package kz.ask.service.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
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
public class BusinessServiceCreateRequest {

    @NotNull
    private UUID categoryId;

    @NotBlank
    private String name;

    private String description;

    private BigDecimal basePrice;

    private String scheduleText;

    private Boolean active;

    private String imageUrl;
}
