package kz.ask.audit.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import kz.ask.audit.domain.entity.SignificantEvent;
import kz.ask.audit.domain.enums.SignificantEventType;
import kz.ask.audit.infrastructure.repository.SignificantEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SignificantEventServiceImpl implements SignificantEventService {

    private final SignificantEventRepository significantEventRepository;

    @Override
    @Transactional
    public void record(UUID actorUserId, SignificantEventType eventType,
                       UUID businessId, UUID entityId, Map<String, Object> metadata) {
        SignificantEvent event = new SignificantEvent();
        event.setActorUserId(actorUserId);
        event.setEventType(eventType);
        event.setBusinessId(businessId);
        event.setEntityId(entityId);
        event.setMetadata(metadata == null ? new HashMap<>() : new HashMap<>(metadata));
        significantEventRepository.save(event);
    }
}
