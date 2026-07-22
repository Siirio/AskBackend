package kz.ask.moderation.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import kz.ask.audit.domain.SignificantEventService;
import kz.ask.audit.domain.enums.SignificantEventType;
import kz.ask.offer.item.domain.entity.Item;

import kz.ask.offer.item.domain.enums.ProductModerationStatus;

import kz.ask.offer.item.infrastructure.repository.ProductRepository;
import kz.ask.identity.infrastructure.repository.AppUserRepository;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.moderation.api.dto.ContentReportResponse;
import kz.ask.moderation.api.dto.CreateContentReportRequest;
import kz.ask.moderation.api.dto.ProductModerationItemResponse;
import kz.ask.moderation.api.dto.RejectProductRequest;
import kz.ask.moderation.infrastructure.repository.ModerationActionRepository;
import kz.ask.platform.domain.PlatformMembershipService;
import kz.ask.platform.domain.dto.PlatformMembershipDto;
import kz.ask.moderation.domain.entity.ModerationAction;
import kz.ask.platform.domain.enums.ModerationStatus;
import kz.ask.platform.domain.enums.ModerationTargetType;
import kz.ask.identity.authorization.domain.enums.Permission;
import kz.ask.search.basic.domain.SearchVisibilityService;

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

    private final ModerationActionRepository moderationActionRepository;
    private final AppUserRepository appUserRepository;
    private final PlatformMembershipService platformMembershipService;
    private final ProductRepository productRepository;
    private final SearchVisibilityService searchVisibilityService;
    private final SignificantEventService significantEventService;

    @Transactional
    public ContentReportResponse report(
            AskPrincipal principal,
            CreateContentReportRequest request) {
        ModerationAction action = new ModerationAction();
        action.setTargetType(request.getTargetType());
        action.setTargetId(request.getTargetId());
        action.setReasonCode(request.getReasonCode());
        action.setDetails(request.getDetails());
        action.setModerationStatus(ModerationStatus.BEING_DISCUSSED);
        action.setMadeBy(appUserRepository.getReferenceById(principal.getUserId()));
        return toResponse(moderationActionRepository.save(action));
    }

    @Transactional(readOnly = true)
    public List<ContentReportResponse> listOpen(AskPrincipal principal) {
        requirePermission(principal, Permission.MODERATE_CONTENT);
        return moderationActionRepository
                .findByModerationStatusOrderByCreatedAtAsc(ModerationStatus.BEING_DISCUSSED)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ContentReportResponse resolve(
            AskPrincipal principal,
            UUID reportId,
            ModerationStatus status,
            String note) {
        requirePermission(principal, Permission.MODERATE_CONTENT);
        if (status != ModerationStatus.VALID
                && status != ModerationStatus.BANNED) {
            throw new ValidationException(ErrorCode.CONTENT_REPORT_INVALID_STATUS);
        }
        ModerationAction action = moderationActionRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.CONTENT_REPORT_NOT_FOUND, reportId));
        if (action.getModerationStatus() != ModerationStatus.BEING_DISCUSSED) {
            throw new ConflictException(ErrorCode.CONTENT_REPORT_ALREADY_RESOLVED);
        }
        action.setModerationStatus(status);
        action.setMadeBy(appUserRepository.getReferenceById(principal.getUserId()));
        action.setNote(note);
        return toResponse(action);
    }

    @Transactional
    public void moderateProduct(
            AskPrincipal principal,
            UUID productId,
            Boolean hidden) {
        requirePermission(principal, Permission.MODERATE_CONTENT);
        Item item = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, productId));
        item.setModerationStatus(Boolean.TRUE.equals(hidden)
                ? ProductModerationStatus.REJECTED
                : ProductModerationStatus.APPROVED);
        searchVisibilityService.republishProductOffers(productId);

        ModerationAction action = new ModerationAction();
        action.setTargetType(ModerationTargetType.PRODUCT);
        action.setTargetId(productId);
        action.setModerationStatus(Boolean.TRUE.equals(hidden)
                ? ModerationStatus.BANNED
                : ModerationStatus.VALID);
        action.setMadeBy(appUserRepository.getReferenceById(principal.getUserId()));
        moderationActionRepository.save(action);

        if (Boolean.TRUE.equals(hidden)) {
            significantEventService.record(principal.getUserId(),
                    SignificantEventType.PRODUCT_HIDDEN_BY_MODERATOR,
                    item.getBusiness().getId(), productId, Map.of());
        }
    }

    private void requirePermission(AskPrincipal principal, Permission permission) {
        PlatformMembershipDto membership =
                platformMembershipService.findActiveByUser(principal.getUserId());
        if (membership == null || !membership.getPermissions().contains(permission)) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
    }

    @Transactional(readOnly = true)
    public Page<ProductModerationItemResponse> listModerationQueue(
            AskPrincipal principal, int page, int size) {
        requirePermission(principal, Permission.MODERATE_CONTENT);
        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 0);
        return productRepository
                .findByModerationStatusOrderByCreatedAtAsc(
                        ProductModerationStatus.PENDING, PageRequest.of(safePage, safeSize))
                .map(this::toModerationItemResponse);
    }

    @Transactional
    public void approveProduct(AskPrincipal principal, UUID productId) {
        requirePermission(principal, Permission.MODERATE_CONTENT);
        Item item = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, productId));
        item.setModerationStatus(ProductModerationStatus.APPROVED);
        searchVisibilityService.republishProductOffers(productId);
    }

    @Transactional
    public void rejectProduct(AskPrincipal principal, UUID productId, RejectProductRequest request) {
        requirePermission(principal, Permission.MODERATE_CONTENT);
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

    private ContentReportResponse toResponse(ModerationAction action) {
        return ContentReportResponse.builder()
                .id(action.getId())
                .targetType(action.getTargetType().name())
                .targetId(action.getTargetId())
                .reasonCode(action.getReasonCode())
                .details(action.getDetails())
                .status(action.getModerationStatus().name())
                .note(action.getNote())
                .createdAt(action.getCreatedAt())
                .build();
    }
}
