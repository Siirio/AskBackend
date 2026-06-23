package kz.ask.service.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Getter
public class UpdateServiceRequest {
    private UUID categoryId;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private Integer durationMinutes;
    private String scheduleText;
    private Boolean active;
}
