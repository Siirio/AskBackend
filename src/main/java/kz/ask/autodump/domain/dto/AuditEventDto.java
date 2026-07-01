package kz.ask.autodump.domain.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuditEventDto {

    private UUID id;
    private UUID importSessionId;
    private UUID draftItemId;
    private UUID actorUserId;
    private String eventType;
    private String payloadJson;
}
