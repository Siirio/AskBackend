package kz.ask.autodump.domain;

import java.util.UUID;
import kz.ask.autodump.domain.enums.AuditEventType;
import kz.ask.autodump.domain.enums.ErrorSeverity;

public interface AutodumpAuditService {

    void recordEvent(UUID sessionId, UUID draftItemId, UUID actorUserId,
                     AuditEventType eventType, String payloadJson);

    void recordError(UUID sessionId, UUID draftItemId, ErrorSeverity severity,
                     String code, String message, String payloadJson);
}
