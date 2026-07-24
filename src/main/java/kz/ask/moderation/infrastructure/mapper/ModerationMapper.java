package kz.ask.moderation.infrastructure.mapper;

import java.time.Instant;
import java.util.UUID;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.moderation.api.dto.ContentReportResponse;
import kz.ask.moderation.api.dto.ModerationActionResponse;
import kz.ask.moderation.domain.entity.ModerationAction;
import kz.ask.platform.domain.enums.ModerationActionType;
import kz.ask.platform.domain.enums.ModerationStatus;
import kz.ask.platform.domain.enums.ModerationTargetType;
import org.springframework.stereotype.Component;

@Component
public class ModerationMapper {

    public void applyCreateFields(ModerationAction entity, ModerationTargetType targetType, UUID targetId,
                                   ModerationActionType action, ModerationStatus moderationStatus,
                                   String reasonCode, String details, String note, Instant expiresAt,
                                   AppUser performedBy) {
        entity.setTargetType(targetType);
        entity.setTargetId(targetId);
        entity.setAction(action);
        entity.setModerationStatus(moderationStatus);
        entity.setReasonCode(reasonCode);
        entity.setDetails(details);
        entity.setNote(note);
        entity.setExpiresAt(expiresAt);
        entity.setPerformedBy(performedBy);
    }

    public void applyResolveFields(ModerationAction entity, ModerationStatus moderationStatus,
                                    ModerationActionType action, AppUser performedBy, String note) {
        entity.setModerationStatus(moderationStatus);
        entity.setAction(action);
        entity.setPerformedBy(performedBy);
        entity.setNote(note);
    }

    public ContentReportResponse toContentReportResponse(ModerationAction entity) {
        return ContentReportResponse.builder()
                .id(entity.getId())
                .targetType(entity.getTargetType().name())
                .targetId(entity.getTargetId())
                .reasonCode(entity.getReasonCode())
                .details(entity.getDetails())
                .status(entity.getModerationStatus().name())
                .note(entity.getNote())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public ModerationActionResponse toModerationActionResponse(ModerationAction entity) {
        return ModerationActionResponse.builder()
                .id(entity.getId())
                .targetType(entity.getTargetType().name())
                .targetId(entity.getTargetId())
                .action(entity.getAction().name())
                .moderationStatus(entity.getModerationStatus().name())
                .note(entity.getNote())
                .expiresAt(entity.getExpiresAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
