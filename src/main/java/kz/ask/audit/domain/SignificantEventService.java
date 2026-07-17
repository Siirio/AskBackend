package kz.ask.audit.domain;

import java.util.Map;
import java.util.UUID;
import kz.ask.audit.domain.enums.SignificantEventType;

public interface SignificantEventService {

    void record(UUID actorUserId, SignificantEventType eventType,
                UUID businessId, UUID entityId, Map<String, Object> metadata);
}
