package kz.ask.search.application;

import java.time.Instant;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.platform.domain.PlatformMembershipService;
import kz.ask.platform.domain.dto.PlatformMembershipDto;
import kz.ask.platform.domain.enums.PlatformPermission;
import kz.ask.search.api.dto.RequestAiEnrichmentRequest;
import kz.ask.search.api.dto.RequestAiEnrichmentResponse;
import kz.ask.search.domain.entity.SearchDocument;
import kz.ask.search.infrastructure.repository.SearchDocumentRepository;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PlatformAiEnrichmentProcessor {

    private final PlatformMembershipService platformMembershipService;
    private final SearchDocumentRepository searchDocumentRepository;

    @Transactional
    public RequestAiEnrichmentResponse request(
            AskPrincipal principal,
            RequestAiEnrichmentRequest request) {
        PlatformMembershipDto membership =
                platformMembershipService.findActiveByUser(principal.getUserId());
        if (membership == null
                || !membership.getPermissions().contains(PlatformPermission.USE_AI_CATALOG_TOOLS)) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
        Instant now = Instant.now();
        var documents = searchDocumentRepository.findByDocumentTypeAndAggregateIdIn(
                request.getDocumentType(), request.getAggregateIds());
        for (SearchDocument document : documents) {
            document.setAiEnrichmentRequested(Boolean.TRUE);
            document.setAiEnrichmentAvailableAt(now);
            document.setAiEnrichmentStartedAt(null);
            document.setAiEnrichmentWorkerId(null);
            document.setAiEnrichmentAttemptCount(0);
            document.setAiEnrichmentError(null);
            document.setAiEnrichmentDead(Boolean.FALSE);
        }
        return RequestAiEnrichmentResponse.builder()
                .queuedCount(documents.size())
                .build();
    }
}
