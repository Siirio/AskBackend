package kz.ask.platform.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlatformAccountResponse {

    private UUID userId;
    private String email;
    private String displayName;
    private String status;
    private List<String> businessNames;
    private Instant createdAt;
}
