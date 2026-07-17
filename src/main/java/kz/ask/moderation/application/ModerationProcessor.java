package kz.ask.moderation.application;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import kz.ask.audit.domain.SignificantEventService;
import kz.ask.audit.domain.enums.SignificantEventType;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.enums.BusinessModerationStatus;
import kz.ask.business.infrastructure.repository.BusinessRepository;
import kz.ask.catalog.domain.dto.ProductDto;
import kz.ask.catalog.domain.service.ProductService;
import kz.ask.identity.infrastructure.repository.AppUserRepository;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.moderation.api.dto.ContentReportResponse;
import kz.ask.moderation.api.dto.CreateContentReportRequest;
import kz.ask.moderation.domain.entity.ContentReport;
import kz.ask.moderation.domain.enums.ContentReportStatus;
import kz.ask.moderation.infrastructure.repository.ContentReportRepository;
import kz.ask.platform.domain.PlatformMembershipService;
import kz.ask.platform.domain.dto.PlatformMembershipDto;
import kz.ask.platform.domain.enums.PlatformPermission;
import kz.ask.search.domain.SearchVisibilityService;
import kz.ask.shared.domain.enums.RecordStatus;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import kz.ask.shared.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ModerationProcessor {

    private final ContentReportRepository contentReportRepository;
    private final BusinessRepository businessRepository;
    private final AppUserRepository appUserRepository;
    private final PlatformMembershipService platformMembershipService;
    private final ProductService productService;
    private final SearchVisibilityService searchVisibilityService;
    private final SignificantEventService significantEventService;

    @Transactional
    public ContentReportResponse report(
            AskPrincipal principal,
            CreateContentReportRequest request) {
        ContentReport report = new ContentReport();
        report.setReporter(appUserRepository.getReferenceById(principal.getUserId()));
        report.setTargetType(request.getTargetType());
        report.setTargetId(request.getTargetId());
        report.setReasonCode(request.getReasonCode());
        report.setDetails(request.getDetails());
        report.setStatus(ContentReportStatus.OPEN);
        return toResponse(contentReportRepository.save(report));
    }

    @Transactional(readOnly = true)
    public List<ContentReportResponse> listOpen(AskPrincipal principal) {
        requirePermission(principal, PlatformPermission.MODERATE_CONTENT);
        return contentReportRepository.findByStatusOrderByCreatedAtAsc(ContentReportStatus.OPEN)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ContentReportResponse resolve(
            AskPrincipal principal,
            UUID reportId,
            ContentReportStatus status) {
        requirePermission(principal, PlatformPermission.MODERATE_CONTENT);
        ContentReport report = contentReportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.CONTENT_REPORT_NOT_FOUND, reportId));
        report.setStatus(status);
        report.setResolvedBy(appUserRepository.getReferenceById(principal.getUserId()));
        report.setResolvedAt(Instant.now());
        return toResponse(report);
    }

    @Transactional
    public void moderateBusiness(
            AskPrincipal principal,
            UUID businessId,
            BusinessModerationStatus status) {
        requirePermission(principal, permissionFor(status));
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.BUSINESS_NOT_FOUND, businessId));
        business.setModerationStatus(status);
        business.setStatus(status == BusinessModerationStatus.VISIBLE
                ? RecordStatus.ACTIVE
                : RecordStatus.INACTIVE);
        searchVisibilityService.republishBusinessOffers(businessId);
        if (status == BusinessModerationStatus.SUSPENDED) {
            significantEventService.record(principal.getUserId(),
                    SignificantEventType.BUSINESS_SUSPENDED, businessId, businessId, Map.of());
        }
        if (status == BusinessModerationStatus.BANNED) {
            significantEventService.record(principal.getUserId(),
                    SignificantEventType.BUSINESS_BANNED, businessId, businessId, Map.of());
        }
    }

    @Transactional
    public void moderateProduct(
            AskPrincipal principal,
            UUID productId,
            Boolean hidden) {
        requirePermission(principal, PlatformPermission.MODERATE_CONTENT);
        ProductDto product = productService.setHiddenByModerator(productId, hidden);
        searchVisibilityService.republishProductOffers(productId);
        if (Boolean.TRUE.equals(hidden)) {
            significantEventService.record(principal.getUserId(),
                    SignificantEventType.PRODUCT_HIDDEN_BY_MODERATOR,
                    product.getBusinessId(), productId, Map.of());
        }
    }

    private PlatformPermission permissionFor(BusinessModerationStatus status) {
        if (status == BusinessModerationStatus.BANNED) {
            return PlatformPermission.BAN_BUSINESS;
        }
        if (status == BusinessModerationStatus.SUSPENDED) {
            return PlatformPermission.SUSPEND_BUSINESS;
        }
        return PlatformPermission.MODERATE_CONTENT;
    }

    private void requirePermission(AskPrincipal principal, PlatformPermission permission) {
        PlatformMembershipDto membership =
                platformMembershipService.findActiveByUser(principal.getUserId());
        if (membership == null || !membership.getPermissions().contains(permission)) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
    }

    private ContentReportResponse toResponse(ContentReport report) {
        return ContentReportResponse.builder()
                .id(report.getId())
                .targetType(report.getTargetType().name())
                .targetId(report.getTargetId())
                .reasonCode(report.getReasonCode())
                .details(report.getDetails())
                .status(report.getStatus().name())
                .reporterUserId(report.getReporter().getId())
                .reporterName(report.getReporter().getDisplayName())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
