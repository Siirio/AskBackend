package kz.ask.business.domain.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BrandDropDto {
    private UUID id;
    private UUID businessId;
    private String name;
    private String description;
    private Instant startDate;
    private Instant endDate;
    private String type;
    private String status;
    private String coverUrl;
}
