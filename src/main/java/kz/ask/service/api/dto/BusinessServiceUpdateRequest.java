package kz.ask.service.api.dto;

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
public class BusinessServiceUpdateRequest {

    private UUID categoryId;

    private String name;

    private String description;

    private BigDecimal basePrice;

    private Integer durationMinutes;

    private String scheduleText;

    private Boolean active;
}
