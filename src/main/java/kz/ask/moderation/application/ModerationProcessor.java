package kz.ask.moderation.application;

import java.time.Instant;
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
import kz.ask.moderation.api.dto.ModerationActionRequest;
import kz.ask.moderation.api.dto.ModerationActionResponse;
import kz.ask.moderation.api.dto.ProductModerationItemResponse;
import kz.ask.moderation.api.dto.RejectProductRequest;
import kz.ask.moderation.infrastructure.mapper.ModerationMapper;
import kz.ask.moderation.infrastructure.repository.ModerationActionRepository;
import kz.ask.offer.item.infrastructure.mapper.ItemMapper;
import kz.ask.platform.domain.PlatformMembershipService;
import kz.ask.platform.domain.dto.PlatformMembershipDto;
import kz.ask.moderation.domain.entity.ModerationAction;
import kz.ask.platform.domain.enums.ModerationActionType;
import kz.ask.platform.domain.enums.ModerationStatus;
import kz.ask.platform.domain.enums.ModerationTargetType;
import kz.ask.identity.authorization.domain.enums.Permission;
import kz.ask.search.basic.domain.SearchDocumentService;
import kz.ask.search.basic.domain.SearchOutboxService;
import kz.ask.search.basic.domain.SearchableItemSource;
import kz.ask.search.basic.domain.enums.SearchAggregateType;
import kz.ask.search.basic.domain.enums.SearchEventType;

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
    private final SearchDocumentService searchDocumentService;
    private final SearchOutboxService searchOutboxService;
    private final SignificantEventService significantEventService;
    private final ModerationMapper moderationMapper;
    private final ItemMapper itemMapper;

    @Transactional
    public ContentReportResponse report(
            AskPrincipal principal,
            CreateContentReportRequest request) {
        ModerationAction action = new ModerationAction();
        moderationMapper.applyCreateFields(action, request.getTargetType(), request.getTargetId(),
                ModerationActionType.FLAG, ModerationStatus.BEING_DISCUSSED,
                request.getReasonCode(), request.getDetails(), null, null,
                appUserRepository.getReferenceById(principal.getUserId()));
        return moderationMapper.toContentReportResponse(moderationActionRepository.save(action));
    }

    @Transactional(readOnly = true)
    public List<ContentReportResponse> listOpen(AskPrincipal principal) {
        requirePermission(principal, Permission.VIEW_MODERATION_QUEUE);
        return moderationActionRepository
                .findByModerationStatusOrderByCreatedAtAsc(ModerationStatus.BEING_DISCUSSED)
                .stream()
                .map(moderationMapper::toContentReportResponse)
                .toList();
    }

    @Transactional
    public ContentReportResponse resolve(
            AskPrincipal principal,
            UUID reportId,
            ModerationStatus status,
            String note) {
        requirePermission(principal, Permission.VIEW_MODERATION_QUEUE);
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
        moderationMapper.applyResolveFields(action, status,
                status == ModerationStatus.BANNED ? ModerationActionType.BLOCK : ModerationActionType.APPROVE,
                appUserRepository.getReferenceById(principal.getUserId()), note);
        return moderationMapper.toContentReportResponse(action);
    }

    @Transactional
    public void moderateProduct(
            AskPrincipal principal,
            UUID productId,
            Boolean hidden) {
        requirePermission(principal, Permission.MODERATE_ITEMS);
        Item item = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, productId));
        ProductModerationStatus newStatus = Boolean.TRUE.equals(hidden)
                ? ProductModerationStatus.REJECTED
                : ProductModerationStatus.APPROVED;
        item.setModerationStatus(newStatus);
        productRepository.save(item);
        if (newStatus == ProductModerationStatus.APPROVED && Boolean.TRUE.equals(item.getIsActive())) {
            Long version = searchDocumentService.upsertItemProjection(buildSearchableItemSource(item));
            searchOutboxService.publish(SearchAggregateType.PRODUCT_OFFER, productId,
                    SearchEventType.UPSERT, version);
        } else {
            searchDocumentService.deleteItemProjection(productId);
            searchOutboxService.publish(SearchAggregateType.PRODUCT_OFFER, productId,
                    SearchEventType.DELETE, Instant.now().toEpochMilli());
        }

        ModerationAction action = new ModerationAction();
        moderationMapper.applyCreateFields(action, ModerationTargetType.PRODUCT, productId,
                Boolean.TRUE.equals(hidden) ? ModerationActionType.BLOCK : ModerationActionType.APPROVE,
                Boolean.TRUE.equals(hidden) ? ModerationStatus.BANNED : ModerationStatus.VALID,
                null, null, null, null,
                appUserRepository.getReferenceById(principal.getUserId()));
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
        requirePermission(principal, Permission.VIEW_MODERATION_QUEUE);
        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 0);
        return productRepository
                .findByModerationStatusOrderByCreatedAtAsc(
                        ProductModerationStatus.PENDING, PageRequest.of(safePage, safeSize))
                .map(itemMapper::toProductModerationItemResponse);
    }

    @Transactional
    public void approveProduct(AskPrincipal principal, UUID productId) {
        requirePermission(principal, Permission.MODERATE_ITEMS);
        Item item = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, productId));
        item.setModerationStatus(ProductModerationStatus.APPROVED);
        productRepository.save(item);
        if (Boolean.TRUE.equals(item.getIsActive())) {
            Long version = searchDocumentService.upsertItemProjection(buildSearchableItemSource(item));
            searchOutboxService.publish(SearchAggregateType.PRODUCT_OFFER, productId,
                    SearchEventType.UPSERT, version);
        }
    }

    @Transactional
    public void rejectProduct(AskPrincipal principal, UUID productId, RejectProductRequest request) {
        requirePermission(principal, Permission.MODERATE_ITEMS);
        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new ValidationException(ErrorCode.MODERATION_REJECT_REASON_REQUIRED);
        }
        Item item = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, productId));
        item.setModerationStatus(ProductModerationStatus.REJECTED);
        item.setModerationNote(request.getReason());
        productRepository.save(item);
        searchDocumentService.deleteItemProjection(productId);
        searchOutboxService.publish(SearchAggregateType.PRODUCT_OFFER, productId,
                SearchEventType.DELETE, Instant.now().toEpochMilli());
        significantEventService.record(principal.getUserId(),
                SignificantEventType.PRODUCT_HIDDEN_BY_MODERATOR,
                item.getBusiness().getId(), productId, Map.of("reason", request.getReason()));
    }

    @Transactional
    public ModerationActionResponse executeModerationAction(AskPrincipal principal, ModerationActionRequest request) {
        Permission requiredPermission = resolvePermission(request.getTargetType());
        requirePermission(principal, requiredPermission);
        ModerationAction action = new ModerationAction();
        moderationMapper.applyCreateFields(action, request.getTargetType(), request.getTargetId(),
                request.getAction(), toModerationStatus(request.getAction()),
                request.getReasonCode(), null, request.getNote(), request.getExpiresAt(),
                appUserRepository.getReferenceById(principal.getUserId()));
        return moderationMapper.toModerationActionResponse(moderationActionRepository.save(action));
    }

    private Permission resolvePermission(ModerationTargetType targetType) {
        return switch (targetType) {
            case PRODUCT -> Permission.MODERATE_ITEMS;
            case SERVICE -> Permission.MODERATE_SERVICES;
            case BUSINESS -> Permission.MODERATE_BUSINESSES;
            case USER -> Permission.MODERATE_APP_USERS;
            case MESSAGE -> Permission.MODERATE_CHATS;
        };
    }

    private SearchableItemSource buildSearchableItemSource(Item item) {
        return SearchableItemSource.builder()
                .itemId(item.getId())
                .businessId(item.getBusiness().getId())
                .branchId(item.getBranch() != null ? item.getBranch().getId() : null)
                .name(item.getName())
                .description(item.getDescription())
                .categoryLabel(item.getCategoryLabel())
                .businessName(item.getBusiness().getName())
                .branchName(item.getBranch() != null ? item.getBranch().getName() : null)
                .price(item.getPrice())
                .tags(item.getTags())
                .attributes(item.getAttributes())
                .latitude(item.getBranch() != null ? item.getBranch().getLatitude() : null)
                .longitude(item.getBranch() != null ? item.getBranch().getLongitude() : null)
                .active(Boolean.TRUE.equals(item.getIsActive()))
                .build();
    }

    private ModerationStatus toModerationStatus(ModerationActionType action) {
        return switch (action) {
            case BLOCK -> ModerationStatus.BANNED;
            case UNBLOCK, APPROVE -> ModerationStatus.VALID;
            case REJECT -> ModerationStatus.BANNED;
            case FLAG -> ModerationStatus.BEING_DISCUSSED;
        };
    }

}
