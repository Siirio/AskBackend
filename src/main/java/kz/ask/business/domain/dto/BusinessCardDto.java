package kz.ask.business.domain.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BusinessCardDto {
    private UUID id;
    private UUID businessId;
    private String blocks;
    private Instant publishedAt;
    private String status;
}
