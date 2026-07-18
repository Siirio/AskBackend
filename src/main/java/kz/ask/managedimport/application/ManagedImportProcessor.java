package kz.ask.managedimport.application;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import kz.ask.business.domain.BusinessMemberService;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.legal.domain.LegalService;
import kz.ask.legal.domain.enums.LegalAcceptanceChannel;
import kz.ask.legal.domain.enums.LegalDocumentCode;
import kz.ask.managedimport.api.dto.CreateManagedImportRequest;
import kz.ask.managedimport.api.dto.ManagedImportAccessResponse;
import kz.ask.managedimport.domain.ManagedImportService;
import kz.ask.managedimport.domain.dto.ManagedImportDto;
import kz.ask.managedimport.domain.enums.ManagedImportStatus;
import kz.ask.platform.domain.PlatformMembershipService;
import kz.ask.platform.domain.dto.PlatformMembershipDto;
import kz.ask.platform.domain.enums.PlatformPermission;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ManagedImportProcessor {

    private final ManagedImportService managedImportService;
    private final BusinessMemberService businessMemberService;
    private final PlatformMembershipService platformMembershipService;
    private final LegalService legalService;

    @Transactional
    public ManagedImportDto create(
            AskPrincipal principal,
            UUID businessId,
            CreateManagedImportRequest request) {
        if (!businessMemberService.isManagerOrAboveOfBusiness(
                businessId, principal.getUserId())) {
            throw new ForbiddenException(ErrorCode.MANAGED_IMPORT_FORBIDDEN);
        }
        legalService.acceptActiveDocuments(
                principal.getUserId(),
                Set.of(LegalDocumentCode.MANAGED_IMPORT_TERMS),
                request.getCountryCode(),
                request.getLocale(),
                LegalAcceptanceChannel.ACCOUNT_SETTINGS);
        return managedImportService.create(
                businessId,
                principal.getUserId(),
                request.getSourceTypes(),
                request.getPreferredContactChannel(),
                request.getPreferredContactValue(),
                request.getSourceLinks(),
                request.getSourceNotes());
    }

    @Transactional(readOnly = true)
    public List<ManagedImportDto> listForBusiness(
            AskPrincipal principal,
            UUID businessId) {
        if (businessMemberService.findByBusinessAndUser(
                businessId, principal.getUserId()) == null) {
            throw new ForbiddenException(ErrorCode.MANAGED_IMPORT_FORBIDDEN);
        }
        return managedImportService.listForBusiness(businessId);
    }

    @Transactional(readOnly = true)
    public List<ManagedImportDto> listPlatform(AskPrincipal principal) {
        requirePermission(principal, PlatformPermission.MANAGE_MANAGED_IMPORTS);
        return managedImportService.listOpen().stream()
                .filter(item -> item.getStatus() == ManagedImportStatus.PENDING
                        || principal.getUserId().equals(item.getResponsiblePlatformUserId())
                        && item.getExpiresAt() != null
                        && item.getExpiresAt().isAfter(Instant.now()))
                .toList();
    }

    @Transactional
    public ManagedImportDto activate(AskPrincipal principal, UUID requestId) {
        requirePermission(principal, PlatformPermission.MANAGE_MANAGED_IMPORTS);
        return managedImportService.activate(requestId, principal.getUserId());
    }

    @Transactional(readOnly = true)
    public ManagedImportAccessResponse catalogAccess(
            AskPrincipal principal,
            UUID businessId) {
        requirePermission(principal, PlatformPermission.EDIT_CATALOG_DURING_IMPORT);
        if (!managedImportService.hasActiveGrant(businessId, principal.getUserId())) {
            throw new ForbiddenException(ErrorCode.MANAGED_IMPORT_FORBIDDEN);
        }
        return ManagedImportAccessResponse.builder().allowed(true).build();
    }

    private void requirePermission(
            AskPrincipal principal,
            PlatformPermission permission) {
        PlatformMembershipDto membership =
                platformMembershipService.findActiveByUser(principal.getUserId());
        if (membership == null || !membership.getPermissions().contains(permission)) {
            throw new ForbiddenException(ErrorCode.MANAGED_IMPORT_FORBIDDEN);
        }
    }
}
