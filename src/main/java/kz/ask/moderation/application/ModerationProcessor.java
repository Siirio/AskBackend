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
import kz.ask.item.domain.entity.Item;
import kz.ask.managedimport.domain.entity.ManagedImportRequest;
import kz.ask.managedimport.domain.enums.ManagedImportStatus;
import kz.ask.managedimport.infrastructure.repository.ManagedImportRequestRepository;

import kz.ask.item.domain.enums.ProductModerationStatus;

import kz.ask.item.infrastructure.repository.ProductRepository;
import kz.ask.identity.infrastructure.repository.AppUserRepository;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.moderation.api.dto.ContentReportResponse;
import kz.ask.moderation.api.dto.CatalogReviewBusinessResponse;
import kz.ask.moderation.api.dto.CreateContentReportRequest;
import kz.ask.moderation.api.dto.ProductModerationItemResponse;
import kz.ask.moderation.api.dto.RejectProductRequest;
import kz.ask.moderation.domain.entity.ContentReport;
import kz.ask.moderation.domain.enums.ContentReportStatus;
import kz.ask.moderation.infrastructure.repository.ContentReportRepository;
import kz.ask.platform.domain.PlatformMembershipService;
import kz.ask.platform.domain.dto.PlatformMembershipDto;
import kz.ask.platform.domain.enums.PlatformPermission;
import kz.ask.search.domain.SearchVisibilityService;

import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import kz.ask.shared.error.ConflictException;
import kz.ask.shared.error.NotFoundException;
import kz.ask.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ModerationProcessor {

    private final ContentReportRepository contentReportRepository;
    private final BusinessRepository businessRepository;
    private final ManagedImportRequestRepository managedImportRequestRepository;
    private final AppUserRepository appUserRepository;
    private final PlatformMembershipService platformMembershipService;
    private final ProductRepository productRepository;
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

    @Transactional(readOnly = true)
    public List<CatalogReviewBusinessResponse> listCatalogReviews(AskPrincipal principal) {
        requirePermission(principal, PlatformPermission.MODERATE_CONTENT);
        return managedImportRequestRepository.findByStatusInOrderByCreatedAtAsc(
                        List.of(ManagedImportStatus.PENDING))
                .stream()
                .map(request -> CatalogReviewBusinessResponse.builder()
                        .businessId(request.getBusiness().getId())
                        .businessName(request.getBusiness().getName())
                        .catalogStatus(request.getStatus().name())
                        .build())
                .toList();
    }

    @Transactional
    public void reviewCatalog(AskPrincipal principal, UUID businessId, Boolean approved) {
        requirePermission(principal, PlatformPermission.MODERATE_CONTENT);
        businessRepository.findById(businessId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.BUSINESS_NOT_FOUND, businessId));
        ManagedImportRequest importRequest = managedImportRequestRepository
                .findByBusinessIdOrderByCreatedAtDesc(businessId)
                .stream().findFirst()
                .orElseThrow(() -> new ConflictException(ErrorCode.CATALOG_SETUP_ALREADY_COMPLETED));
        if (importRequest.getStatus() != ManagedImportStatus.PENDING) {
            throw new ConflictException(ErrorCode.CATALOG_SETUP_ALREADY_COMPLETED);
        }
        importRequest.setStatus(Boolean.TRUE.equals(approved)
                ? ManagedImportStatus.COMPLETED
                : ManagedImportStatus.ACTIVE);
        if (Boolean.TRUE.equals(approved)) {
            importRequest.setCompletedAt(Instant.now());
        }
        searchVisibilityService.republishBusinessOffers(businessId);
    }

    @Transactional
    public ContentReportResponse resolve(
            AskPrincipal principal,
            UUID reportId,
            ContentReportStatus status,
            String resolution) {
        requirePermission(principal, PlatformPermission.MODERATE_CONTENT);
        if (status != ContentReportStatus.RESOLVED
                && status != ContentReportStatus.REJECTED) {
            throw new ValidationException(ErrorCode.CONTENT_REPORT_INVALID_STATUS);
        }
        ContentReport report = contentReportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.CONTENT_REPORT_NOT_FOUND, reportId));
        if (report.getStatus() != ContentReportStatus.OPEN) {
            throw new ConflictException(ErrorCode.CONTENT_REPORT_ALREADY_RESOLVED);
        }
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
        Item item = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, productId));
        item.setModerationStatus(Boolean.TRUE.equals(hidden)
                ? ProductModerationStatus.REJECTED
                : ProductModerationStatus.APPROVED);
        searchVisibilityService.republishProductOffers(productId);
        if (Boolean.TRUE.equals(hidden)) {
            significantEventService.record(principal.getUserId(),
                    SignificantEventType.PRODUCT_HIDDEN_BY_MODERATOR,
                    item.getBusiness().getId(), productId, Map.of());
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

    @Transactional(readOnly = true)
    public Page<ProductModerationItemResponse> listModerationQueue(
            AskPrincipal principal, int page, int size) {
        requirePermission(principal, PlatformPermission.MODERATE_CONTENT);
        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 0);
        return productRepository
                .findByModerationStatusOrderByCreatedAtAsc(
                        ProductModerationStatus.PENDING, PageRequest.of(safePage, safeSize))
                .map(this::toModerationItemResponse);
    }

    @Transactional
    public void approveProduct(AskPrincipal principal, UUID productId) {
        requirePermission(principal, PlatformPermission.MODERATE_CONTENT);
        Item item = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, productId));
        item.setModerationStatus(ProductModerationStatus.APPROVED);
        searchVisibilityService.republishProductOffers(productId);
    }

    @Transactional
    public void rejectProduct(AskPrincipal principal, UUID productId, RejectProductRequest request) {
        requirePermission(principal, PlatformPermission.MODERATE_CONTENT);
        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new ValidationException(ErrorCode.MODERATION_REJECT_REASON_REQUIRED);
        }
        Item item = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, productId));
        item.setModerationStatus(ProductModerationStatus.REJECTED);
        item.setModerationNote(request.getReason());
        searchVisibilityService.republishProductOffers(productId);
        significantEventService.record(principal.getUserId(),
                SignificantEventType.PRODUCT_HIDDEN_BY_MODERATOR,
                item.getBusiness().getId(), productId, Map.of("reason", request.getReason()));
    }

    private ProductModerationItemResponse toModerationItemResponse(Item item) {
        return ProductModerationItemResponse.builder()
                .productId(item.getId())
                .productName(item.getName())
                .businessId(item.getBusiness().getId())
                .createdAt(item.getCreatedAt())
                .moderationNote(item.getModerationNote())
                .moderationStatus(item.getModerationStatus().name())
                .build();
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
                .resolvedAt(report.getResolvedAt())
                .build();
    }
}
