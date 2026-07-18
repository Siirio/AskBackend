package kz.ask.managedimport.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import kz.ask.audit.domain.SignificantEventService;
import kz.ask.audit.domain.enums.SignificantEventType;
import kz.ask.business.domain.enums.CatalogSourceType;
import kz.ask.business.domain.enums.PreferredContactChannel;
import kz.ask.business.infrastructure.repository.BusinessRepository;
import kz.ask.catalog.infrastructure.repository.ProductOfferRepository;
import kz.ask.chat.api.dto.ChatConversationDto;
import kz.ask.chat.domain.ChatService;
import kz.ask.identity.infrastructure.repository.AppUserRepository;
import kz.ask.managedimport.domain.dto.ManagedImportDto;
import kz.ask.managedimport.domain.entity.ManagedImportGrant;
import kz.ask.managedimport.domain.entity.ManagedImportRequest;
import kz.ask.managedimport.domain.enums.ManagedImportGrantStatus;
import kz.ask.managedimport.domain.enums.ManagedImportStatus;
import kz.ask.managedimport.infrastructure.repository.ManagedImportGrantRepository;
import kz.ask.managedimport.infrastructure.repository.ManagedImportRequestRepository;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ConflictException;
import kz.ask.shared.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ManagedImportServiceImpl implements ManagedImportService {

    private final ManagedImportRequestRepository requestRepository;
    private final ManagedImportGrantRepository grantRepository;
    private final BusinessRepository businessRepository;
    private final AppUserRepository appUserRepository;
    private final ProductOfferRepository productOfferRepository;
    private final ChatService chatService;
    private final SignificantEventService significantEventService;

    @Value("${business.managed-import.chat-subject:Catalog import}")
    private String chatSubject;

    @Value("${business.managed-import.initial-message:Your managed catalog import request was sent. An Ask team member will join this chat and help prepare the catalog.}")
    private String initialMessage;

    @Value("${business.managed-import.access-duration:P7D}")
    private Duration accessDuration;

    @Override
    @Transactional
    public ManagedImportDto create(
            UUID businessId,
            UUID requestedByUserId,
            Set<CatalogSourceType> sourceTypes,
            PreferredContactChannel preferredContactChannel,
            String preferredContactValue,
            String sourceLinks,
            String sourceNotes) {
        if (requestRepository.existsByBusinessIdAndStatusIn(
                businessId, List.of(ManagedImportStatus.PENDING, ManagedImportStatus.ACTIVE))) {
            throw new ConflictException(ErrorCode.MANAGED_IMPORT_ACTIVE_EXISTS);
        }
        ManagedImportRequest request = new ManagedImportRequest();
        request.setBusiness(businessRepository.getReferenceById(businessId));
        request.setRequestedBy(appUserRepository.getReferenceById(requestedByUserId));
        request.setStatus(ManagedImportStatus.PENDING);
        request.setSelectedSourceTypes(sourceTypes);
        request.setPreferredContactChannel(preferredContactChannel);
        request.setPreferredContactValue(preferredContactValue);
        request.setSourceLinks(sourceLinks);
        request.setSourceNotes(sourceNotes);
        request = requestRepository.save(request);

        significantEventService.record(requestedByUserId,
                SignificantEventType.MANAGED_IMPORT_REQUESTED,
                businessId, request.getId(),
                Map.of("sourceTypes", sourceTypeNames(sourceTypes)));
        return toDto(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ManagedImportDto> listOpen() {
        return requestRepository.findByStatusInOrderByCreatedAtAsc(
                        List.of(ManagedImportStatus.PENDING, ManagedImportStatus.ACTIVE))
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ManagedImportDto> listForBusiness(UUID businessId) {
        return requestRepository.findByBusinessIdOrderByCreatedAtDesc(businessId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ManagedImportDto activate(UUID requestId, UUID platformUserId) {
        ManagedImportRequest request = requestRepository.findForUpdateById(requestId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.MANAGED_IMPORT_NOT_FOUND, requestId));
        if (request.getStatus() != ManagedImportStatus.PENDING) {
            if (request.getResponsiblePlatformUser() != null
                    && request.getResponsiblePlatformUser().getId().equals(platformUserId)) {
                return toDto(request);
            }
            throw new ConflictException(ErrorCode.MANAGED_IMPORT_ACTIVE_EXISTS);
        }
        if (request.getStatus() == ManagedImportStatus.PENDING) {
            Instant activatedAt = Instant.now();
            request.setStatus(ManagedImportStatus.ACTIVE);
            request.setActivatedAt(activatedAt);
            request.setExpiresAt(activatedAt.plus(accessDuration));
            request.setResponsiblePlatformUser(
                    appUserRepository.getReferenceById(platformUserId));

            ManagedImportGrant grant = new ManagedImportGrant();
            grant.setBusiness(request.getBusiness());
            grant.setManagedImportRequest(request);
            grant.setGrantedBy(request.getResponsiblePlatformUser());
            grant.setStatus(ManagedImportGrantStatus.ACTIVE);
            grant.setGrantedAt(activatedAt);
            grantRepository.save(grant);
            significantEventService.record(platformUserId,
                    SignificantEventType.MANAGED_IMPORT_GRANT_CREATED,
                    request.getBusiness().getId(), grant.getId(), Map.of());

            ChatConversationDto conversation = chatService.startManagedImportConversation(
                    request.getRequestedBy().getId(),
                    request.getBusiness().getId(),
                    request.getId(),
                    chatSubject,
                    initialMessage);
            request.setConversationId(conversation.getConversationId());
            significantEventService.record(platformUserId,
                    SignificantEventType.MANAGED_IMPORT_STARTED,
                    request.getBusiness().getId(), requestId, Map.of());
        }
        return toDto(request);
    }

    @Override
    @Transactional
    public void expireDue(Instant now) {
        requestRepository.findByStatusAndExpiresAtLessThanEqual(ManagedImportStatus.ACTIVE, now)
                .forEach(request -> completeExpired(request, now));
    }

    private void completeExpired(ManagedImportRequest request, Instant completedAt) {
        UUID platformUserId = request.getResponsiblePlatformUser().getId();
        int productsPublishedCount = Math.toIntExact(
                productOfferRepository.countActiveProductsByBusinessId(request.getBusiness().getId()));
        request.setStatus(ManagedImportStatus.COMPLETED);
        request.setCompletedAt(completedAt);
        request.setProductsPublishedCount(productsPublishedCount);
        grantRepository.findByManagedImportRequestIdAndStatus(
                        request.getId(), ManagedImportGrantStatus.ACTIVE)
                .ifPresent(grant -> {
                    grant.setStatus(ManagedImportGrantStatus.REVOKED);
                    grant.setRevokedAt(completedAt);
                    significantEventService.record(platformUserId,
                            SignificantEventType.MANAGED_IMPORT_GRANT_REVOKED,
                            request.getBusiness().getId(), grant.getId(), Map.of());
                });
        if (request.getConversationId() != null) {
            chatService.deleteConversation(request.getConversationId());
            request.setConversationId(null);
        }
        request.setSourceLinks(null);
        request.setSourceNotes(null);
        significantEventService.record(platformUserId,
                SignificantEventType.MANAGED_IMPORT_COMPLETED,
                request.getBusiness().getId(), request.getId(),
                Map.of(
                        "productsPublishedCount", productsPublishedCount,
                        "sourceTypes", sourceTypeNames(request.getSelectedSourceTypes()),
                        "responsiblePlatformUserId", platformUserId.toString()));
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean hasActiveGrant(UUID businessId, UUID platformUserId) {
        return grantRepository.findActiveAccess(businessId, platformUserId, Instant.now()).isPresent();
    }

    private ManagedImportRequest find(UUID requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.MANAGED_IMPORT_NOT_FOUND, requestId));
    }

    private List<String> sourceTypeNames(Set<CatalogSourceType> sourceTypes) {
        return sourceTypes == null
                ? List.of()
                : sourceTypes.stream().map(CatalogSourceType::name).toList();
    }

    private ManagedImportDto toDto(ManagedImportRequest request) {
        return ManagedImportDto.builder()
                .id(request.getId())
                .businessId(request.getBusiness().getId())
                .businessName(request.getBusiness().getName())
                .requestedByUserId(request.getRequestedBy().getId())
                .requestedByName(request.getRequestedBy().getDisplayName())
                .status(request.getStatus())
                .sourceTypes(request.getSelectedSourceTypes())
                .preferredContactChannel(request.getPreferredContactChannel())
                .preferredContactValue(request.getPreferredContactValue())
                .sourceLinks(request.getSourceLinks())
                .sourceNotes(request.getSourceNotes())
                .conversationId(request.getConversationId())
                .responsiblePlatformUserId(request.getResponsiblePlatformUser() != null
                        ? request.getResponsiblePlatformUser().getId()
                        : null)
                .createdAt(request.getCreatedAt())
                .activatedAt(request.getActivatedAt())
                .expiresAt(request.getExpiresAt())
                .completedAt(request.getCompletedAt())
                .build();
    }
}
