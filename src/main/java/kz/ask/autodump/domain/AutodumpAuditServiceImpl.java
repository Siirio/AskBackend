package kz.ask.autodump.domain;

import java.util.UUID;
import kz.ask.autodump.domain.enums.AuditEventType;
import kz.ask.autodump.domain.enums.ErrorSeverity;
import kz.ask.autodump.infrastructure.mapper.AutodumpMapper;
import kz.ask.autodump.infrastructure.repository.AutodumpAuditEventRepository;
import kz.ask.autodump.infrastructure.repository.AutodumpDraftItemRepository;
import kz.ask.autodump.infrastructure.repository.AutodumpImportErrorRepository;
import kz.ask.autodump.infrastructure.repository.AutodumpImportSessionRepository;
import kz.ask.identity.infrastructure.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AutodumpAuditServiceImpl implements AutodumpAuditService {

    private final AutodumpAuditEventRepository auditEventRepository;
    private final AutodumpImportErrorRepository importErrorRepository;
    private final AutodumpImportSessionRepository sessionRepository;
    private final AutodumpDraftItemRepository draftItemRepository;
    private final AppUserRepository appUserRepository;
    private final AutodumpMapper mapper;

    @Override
    @Transactional
    public void recordEvent(UUID sessionId, UUID draftItemId, UUID actorUserId,
                             AuditEventType eventType, String payloadJson) {
        var session = sessionRepository.getReferenceById(sessionId);
        var draftItem = draftItemId != null ? draftItemRepository.getReferenceById(draftItemId) : null;
        var actor = actorUserId != null ? appUserRepository.getReferenceById(actorUserId) : null;
        var entity = mapper.toAuditEventEntity(session, draftItem, actor, eventType, payloadJson);
        auditEventRepository.save(entity);
    }

    @Override
    @Transactional
    public void recordError(UUID sessionId, UUID draftItemId, ErrorSeverity severity,
                             String code, String message, String payloadJson) {
        var session = sessionRepository.getReferenceById(sessionId);
        var draftItem = draftItemId != null ? draftItemRepository.getReferenceById(draftItemId) : null;
        var entity = mapper.toImportErrorEntity(session, draftItem, severity, code, message, payloadJson);
        importErrorRepository.save(entity);
    }
}
