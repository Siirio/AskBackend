package kz.ask.moderation.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import kz.ask.audit.domain.SignificantEventService;
import kz.ask.audit.domain.enums.SignificantEventType;
import kz.ask.business.core.infrastructure.repository.BusinessRepository;
import kz.ask.business.uniqueoffer.domain.entity.UniqueOffer;
import kz.ask.business.uniqueoffer.infrastructure.repository.UniqueOfferRepository;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.identity.domain.enums.UserStatus;
import kz.ask.offer.item.domain.entity.Item;
import kz.ask.offer.item.domain.enums.ProductModerationStatus;
import kz.ask.offer.item.infrastructure.repository.ProductRepository;
import kz.ask.offer.service.domain.entity.Service;
import kz.ask.offer.service.infrastructure.repository.ServiceOfferingRepository;
import kz.ask.identity.infrastructure.repository.AppUserRepository;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.moderation.api.dto.ContentReportResponse;
import kz.ask.moderation.api.dto.CreateContentReportRequest;
import kz.ask.moderation.api.dto.ModerationActionRequest;
import kz.ask.moderation.api.dto.ModerationActionResponse;
import kz.ask.moderation.api.dto.ProductModerationItemResponse;
import kz.ask.moderation.api.dto.RejectProductRequest;
import kz.ask.moderation.domain.ModerationAssessment;
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
import kz.ask.identity.authorization.domain.enums.Role;
import kz.ask.search.basic.application.SearchProjectionComposer;
import kz.ask.search.basic.domain.SearchDocumentService;
import kz.ask.search.basic.domain.SearchOutboxService;
import kz.ask.search.basic.domain.dto.SearchDocumentDto;
import kz.ask.search.basic.domain.enums.SearchAggregateType;
import kz.ask.search.basic.domain.enums.SearchDocumentType;
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
    private final BusinessRepository businessRepository;
    private final ProductRepository productRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;
    private final UniqueOfferRepository uniqueOfferRepository;
    private final SearchDocumentService searchDocumentService;
    private final SearchOutboxService searchOutboxService;
    private final SearchProjectionComposer searchProjectionComposer;
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
        syncSearchProjection(item, newStatus);

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
        syncSearchProjection(item, ProductModerationStatus.APPROVED);
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
        Long version = searchDocumentService.delete(SearchDocumentType.ITEM, productId);
        searchOutboxService.publish(SearchAggregateType.ITEM, productId,
                SearchEventType.DELETE, version);
        significantEventService.record(principal.getUserId(),
                SignificantEventType.PRODUCT_HIDDEN_BY_MODERATOR,
                item.getBusiness().getId(), productId, Map.of("reason", request.getReason()));
    }

    @Transactional
    public ModerationActionResponse executeModerationAction(AskPrincipal principal, ModerationActionRequest request) {
        Permission requiredPermission = resolvePermission(request.getTargetType());
        requirePermission(principal, requiredPermission);
        if ("SOFT_DELETE".equals(request.getReasonCode())) {
            requireRole(principal, Role.SUPER_ADMIN);
        }
        applyTargetState(request);
        resolveOpenAutomatedFlags(principal, request);
        ModerationAction action = new ModerationAction();
        moderationMapper.applyCreateFields(action, request.getTargetType(), request.getTargetId(),
                request.getAction(), toModerationStatus(request.getAction()),
                request.getReasonCode(), null, request.getNote(), request.getExpiresAt(),
                appUserRepository.getReferenceById(principal.getUserId()));
        return moderationMapper.toModerationActionResponse(moderationActionRepository.save(action));
    }

    @Transactional
    public void flagAutomated(
            AskPrincipal principal,
            ModerationTargetType targetType,
            UUID targetId,
            ModerationAssessment assessment) {
        if (!assessment.requiresAction()
                || moderationActionRepository.existsByTargetTypeAndTargetIdAndModerationStatus(
                        targetType, targetId, ModerationStatus.BEING_DISCUSSED)) {
            return;
        }
        ModerationAction action = new ModerationAction();
        moderationMapper.applyCreateFields(
                action,
                targetType,
                targetId,
                ModerationActionType.FLAG,
                ModerationStatus.BEING_DISCUSSED,
                assessment.reasonCode(),
                assessment.matchedSignal(),
                null,
                null,
                appUserRepository.getReferenceById(principal.getUserId()));
        moderationActionRepository.save(action);
    }

    private void resolveOpenAutomatedFlags(
            AskPrincipal principal,
            ModerationActionRequest request) {
        ModerationStatus status = toModerationStatus(request.getAction());
        if (status == ModerationStatus.BEING_DISCUSSED) {
            return;
        }
        AppUser performedBy = appUserRepository.getReferenceById(principal.getUserId());
        moderationActionRepository
                .findByTargetTypeAndTargetIdAndModerationStatus(
                        request.getTargetType(),
                        request.getTargetId(),
                        ModerationStatus.BEING_DISCUSSED)
                .forEach(action -> moderationMapper.applyResolveFields(
                        action,
                        status,
                        request.getAction(),
                        performedBy,
                        request.getNote()));
    }

    private void applyTargetState(ModerationActionRequest request) {
        boolean blocked = request.getAction() == ModerationActionType.BLOCK
                || request.getAction() == ModerationActionType.REJECT;
        switch (request.getTargetType()) {
            case PRODUCT -> {
                Item item = productRepository.findById(request.getTargetId())
                        .orElseThrow(() -> new NotFoundException(
                                ErrorCode.PRODUCT_NOT_FOUND, request.getTargetId()));
                item.setIsActive(!blocked);
                item.setModerationStatus(blocked
                        ? ProductModerationStatus.REJECTED
                        : ProductModerationStatus.APPROVED);
                if (blocked) {
                    item.setModerationNote(request.getNote());
                }
                productRepository.save(item);
                syncSearchProjection(item, item.getModerationStatus());
            }
            case SERVICE -> {
                Service service = serviceOfferingRepository.findById(request.getTargetId())
                        .orElseThrow(() -> new NotFoundException(
                                ErrorCode.REQUEST_NOT_FOUND, request.getTargetId()));
                service.setIsActive(!blocked);
                serviceOfferingRepository.save(service);
                syncServiceProjection(service);
            }
            case UNIQUE_OFFER -> {
                UniqueOffer offer = uniqueOfferRepository.findById(request.getTargetId())
                        .orElseThrow(() -> new NotFoundException(
                                ErrorCode.DROP_NOT_FOUND, request.getTargetId()));
                offer.setIsActive(!blocked);
                offer.setStatus(blocked
                        ? kz.ask.business.uniqueoffer.domain.enums.UniqueOfferStatus.CANCELLED
                        : kz.ask.business.uniqueoffer.domain.enums.UniqueOfferStatus.ACTIVE);
                uniqueOfferRepository.save(offer);
            }
            case BUSINESS -> {
                if (!businessRepository.existsById(request.getTargetId())) {
                    throw new NotFoundException(ErrorCode.BUSINESS_NOT_FOUND, request.getTargetId());
                }
                syncBusinessSearch(request.getTargetId(), blocked);
            }
            case USER -> {
                AppUser user = appUserRepository.findById(request.getTargetId())
                        .orElseThrow(() -> new NotFoundException(
                                ErrorCode.USER_NOT_FOUND, request.getTargetId()));
                user.setStatus(blocked ? UserStatus.BLOCKED : UserStatus.ACTIVE);
                appUserRepository.save(user);
            }
            case MESSAGE -> {
            }
        }
    }

    private void syncSearchProjection(Item item, ProductModerationStatus newStatus) {
        UUID aggregateId = item.getId();
        boolean searchable = Boolean.TRUE.equals(item.getIsActive())
                && newStatus == ProductModerationStatus.APPROVED;
        if (searchable) {
            SearchDocumentDto projection = searchProjectionComposer.composeItem(
                    aggregateId,
                    item.getBusiness().getId(),
                    item.getBranch() != null ? item.getBranch().getId() : null,
                    item.getName(),
                    item.getDescription(),
                    item.getCategoryLabel(),
                    item.getBusiness().getName(),
                    item.getBranch() != null ? item.getBranch().getName() : null,
                    item.getPrice(),
                    item.getBusiness().getCurrency(),
                    item.getTags(),
                    item.getAttributes(),
                    item.getBranch() != null ? item.getBranch().getLatitude() : null,
                    item.getBranch() != null ? item.getBranch().getLongitude() : null);
            Long version = searchDocumentService.upsert(projection);
            searchOutboxService.publish(SearchAggregateType.ITEM, aggregateId,
                    SearchEventType.UPSERT, version);
        } else {
            Long version = searchDocumentService.delete(SearchDocumentType.ITEM, aggregateId);
            searchOutboxService.publish(SearchAggregateType.ITEM, aggregateId,
                    SearchEventType.DELETE, version);
        }
    }

    private void syncServiceProjection(Service service) {
        UUID aggregateId = service.getId();
        if (Boolean.TRUE.equals(service.getIsActive())) {
            SearchDocumentDto projection = searchProjectionComposer.composeService(
                    aggregateId,
                    service.getBusiness().getId(),
                    service.getBranch() != null ? service.getBranch().getId() : null,
                    service.getName(),
                    service.getDescription(),
                    service.getCategoryLabel(),
                    service.getBusiness().getName(),
                    service.getBranch() != null ? service.getBranch().getName() : null,
                    service.getBasePrice(),
                    service.getBusiness().getCurrency(),
                    service.getAttributes(),
                    service.getBranch() != null ? service.getBranch().getLatitude() : null,
                    service.getBranch() != null ? service.getBranch().getLongitude() : null);
            Long version = searchDocumentService.upsert(projection);
            searchOutboxService.publish(SearchAggregateType.SERVICE, aggregateId,
                    SearchEventType.UPSERT, version);
        } else {
            Long version = searchDocumentService.delete(SearchDocumentType.SERVICE, aggregateId);
            searchOutboxService.publish(SearchAggregateType.SERVICE, aggregateId,
                    SearchEventType.DELETE, version);
        }
    }

    private void syncBusinessSearch(UUID businessId, boolean blocked) {
        for (Item item : productRepository.findAllByBusinessId(businessId)) {
            if (blocked) {
                Long version = searchDocumentService.delete(SearchDocumentType.ITEM, item.getId());
                searchOutboxService.publish(SearchAggregateType.ITEM, item.getId(),
                        SearchEventType.DELETE, version);
            } else {
                syncSearchProjection(item, item.getModerationStatus());
            }
        }
        for (Service service : serviceOfferingRepository.findAllByBusinessId(businessId)) {
            if (blocked) {
                Long version = searchDocumentService.delete(SearchDocumentType.SERVICE, service.getId());
                searchOutboxService.publish(SearchAggregateType.SERVICE, service.getId(),
                        SearchEventType.DELETE, version);
            } else {
                syncServiceProjection(service);
            }
        }
    }

    private void requirePermission(AskPrincipal principal, Permission permission) {
        PlatformMembershipDto membership =
                platformMembershipService.findActiveByUser(principal.getUserId());
        if (membership == null || !membership.getPermissions().contains(permission)) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
    }

    private void requireRole(AskPrincipal principal, Role role) {
        PlatformMembershipDto membership =
                platformMembershipService.findActiveByUser(principal.getUserId());
        if (membership == null || membership.getRole() != role) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
    }

    private Permission resolvePermission(ModerationTargetType targetType) {
        return switch (targetType) {
            case PRODUCT -> Permission.MODERATE_ITEMS;
            case SERVICE -> Permission.MODERATE_SERVICES;
            case UNIQUE_OFFER -> Permission.MODERATE_UNIQUE_OFFERS;
            case BUSINESS -> Permission.MODERATE_BUSINESSES;
            case USER -> Permission.MODERATE_APP_USERS;
            case MESSAGE -> Permission.MODERATE_CHATS;
        };
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
