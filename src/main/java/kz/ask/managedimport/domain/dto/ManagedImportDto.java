package kz.ask.managedimport.domain.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import kz.ask.business.domain.enums.CatalogSourceType;
import kz.ask.business.domain.enums.PreferredContactChannel;
import kz.ask.managedimport.domain.enums.ManagedImportStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ManagedImportDto {

    private UUID id;
    private UUID businessId;
    private String businessName;
    private UUID requestedByUserId;
    private String requestedByName;
    private ManagedImportStatus status;
    private Set<CatalogSourceType> sourceTypes;
    private PreferredContactChannel preferredContactChannel;
    private String preferredContactValue;
    private String sourceLinks;
    private String sourceNotes;
    private UUID conversationId;
    private UUID responsiblePlatformUserId;
    private Instant createdAt;
    private Instant activatedAt;
    private Instant expiresAt;
    private Instant completedAt;
}
