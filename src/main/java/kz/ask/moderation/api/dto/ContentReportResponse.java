package kz.ask.moderation.api.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContentReportResponse {

    private UUID id;
    private String targetType;
    private UUID targetId;
    private String reasonCode;
    private String details;
    private String status;
    private String resolution;
    private UUID reporterUserId;
    private String reporterName;
    private Instant createdAt;
    private Instant resolvedAt;
}
