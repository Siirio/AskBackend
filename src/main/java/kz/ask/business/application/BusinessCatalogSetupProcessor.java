package kz.ask.business.application;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import kz.ask.business.api.dto.BusinessCatalogStatusResponse;
import kz.ask.business.domain.BusinessMemberService;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.entity.BusinessVerification;
import kz.ask.business.infrastructure.repository.BusinessRepository;
import kz.ask.business.infrastructure.repository.BusinessVerificationRepository;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.managedimport.domain.ManagedImportService;
import kz.ask.managedimport.domain.entity.ManagedImportRequest;
import kz.ask.managedimport.domain.enums.ManagedImportStatus;
import kz.ask.managedimport.infrastructure.repository.ManagedImportRequestRepository;
import kz.ask.platform.domain.PlatformMembershipService;
import kz.ask.platform.domain.dto.PlatformMembershipDto;
import kz.ask.platform.domain.enums.PlatformPermission;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import kz.ask.shared.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class BusinessCatalogSetupProcessor {

    private final BusinessRepository businessRepository;
    private final BusinessVerificationRepository verificationRepository;
    private final ManagedImportRequestRepository managedImportRequestRepository;
    private final BusinessMemberService businessMemberService;
    private final ManagedImportService managedImportService;
    private final PlatformMembershipService platformMembershipService;

    @Transactional(readOnly = true)
    public BusinessCatalogStatusResponse status(AskPrincipal principal, UUID businessId) {
        requireAccess(principal, businessId);
        return toResponse(findBusiness(businessId));
    }

    private void requireAccess(AskPrincipal principal, UUID businessId) {
        if (businessMemberService.findByBusinessAndUser(
                businessId, principal.getUserId()) != null || hasPlatformGrant(principal, businessId)) {
            return;
        }
        throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
    }

    private boolean hasPlatformGrant(AskPrincipal principal, UUID businessId) {
        PlatformMembershipDto membership =
                platformMembershipService.findActiveByUser(principal.getUserId());
        return membership != null
                && membership.getPermissions().contains(
                        PlatformPermission.EDIT_CATALOG_DURING_IMPORT)
                && managedImportService.hasActiveGrant(businessId, principal.getUserId());
    }

    private Business findBusiness(UUID businessId) {
        return businessRepository.findById(businessId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.BUSINESS_NOT_FOUND, businessId));
    }

    private BusinessCatalogStatusResponse toResponse(Business business) {
        Optional<BusinessVerification> verification = verificationRepository.findByBusinessId(business.getId());
        Optional<ManagedImportRequest> importRequest =
                managedImportRequestRepository.findByBusinessIdOrderByCreatedAtDesc(business.getId())
                        .stream().findFirst();
        ManagedImportStatus importStatus = importRequest.map(ManagedImportRequest::getStatus).orElse(null);
        Instant startedAt = importRequest.map(ManagedImportRequest::getActivatedAt).orElse(null);
        Instant deadlineAt = importRequest.map(ManagedImportRequest::getExpiresAt).orElse(null);
        Instant completedAt = importRequest.map(ManagedImportRequest::getCompletedAt).orElse(null);
        Instant deadlineForStatus = importStatus == ManagedImportStatus.ACTIVE
                ? deadlineAt
                : null;
        return BusinessCatalogStatusResponse.builder()
                .businessId(business.getId())
                .catalogStatus(importStatus != null ? importStatus.name() : null)
                .deadlineAt(deadlineForStatus)
                .verificationStatus(verification.map(v -> v.getStatus().name()).orElse(null))
                .catalogSetupStartedAt(startedAt)
                .catalogSetupDeadlineAt(deadlineAt)
                .catalogSetupCompletedAt(completedAt)
                .catalogReady(importStatus == ManagedImportStatus.COMPLETED
                        && verification.map(v -> v.getStatus().name()).orElse("").equals("APPROVED"))
                .build();
    }
}
