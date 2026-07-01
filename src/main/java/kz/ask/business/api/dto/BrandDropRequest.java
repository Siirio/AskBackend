package kz.ask.business.api.dto;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BrandDropRequest {
    private String name;
    private String description;
    private Instant startDate;
    private Instant endDate;
    private String type;
    private String status;
    private String coverUrl;
}
