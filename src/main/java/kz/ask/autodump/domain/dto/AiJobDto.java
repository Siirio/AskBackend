package kz.ask.autodump.domain.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AiJobDto {

    private UUID id;
    private UUID importSessionId;
    private UUID rawInputId;
    private String status;
    private String provider;
    private String model;
    private String promptVersion;
    private Integer inputTokenEstimate;
    private Integer outputTokenEstimate;
    private String rawResponseJson;
    private String errorMessage;
    private Integer attemptCount;
    private Instant startedAt;
    private Instant finishedAt;
}
