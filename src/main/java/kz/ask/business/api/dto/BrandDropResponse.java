package kz.ask.business.api.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BrandDropResponse {
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
