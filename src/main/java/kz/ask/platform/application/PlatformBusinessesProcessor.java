package kz.ask.platform.application;

import java.util.List;
import java.util.UUID;
import kz.ask.business.branch.domain.entity.BusinessBranch;
import kz.ask.business.branch.infrastructure.repository.BusinessBranchRepository;
import kz.ask.business.core.domain.entity.Business;
import kz.ask.business.core.infrastructure.repository.BusinessRepository;
import kz.ask.business.member.domain.entity.BusinessMember;
import kz.ask.business.member.infrastructure.repository.BusinessMemberRepository;
import kz.ask.business.uniqueoffer.infrastructure.repository.UniqueOfferRepository;
import kz.ask.identity.authorization.domain.enums.Role;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.moderation.domain.entity.ModerationAction;
import kz.ask.moderation.infrastructure.repository.ModerationActionRepository;
import kz.ask.offer.item.api.dto.BusinessProductListResponse;
import kz.ask.offer.item.domain.entity.Item;
import kz.ask.offer.item.domain.enums.ProductModerationStatus;
import kz.ask.offer.item.infrastructure.mapper.ItemMapper;
import kz.ask.offer.item.infrastructure.repository.ProductRepository;
import kz.ask.offer.service.api.dto.BusinessServiceListResponse;
import kz.ask.offer.service.api.dto.BusinessServiceRowResponse;
import kz.ask.offer.service.domain.entity.Service;
import kz.ask.offer.service.infrastructure.repository.ServiceOfferingRepository;
import kz.ask.platform.api.dto.PlatformBusinessBranchResponse;
import kz.ask.platform.api.dto.PlatformBusinessDetailResponse;
import kz.ask.platform.api.dto.PlatformBusinessListResponse;
import kz.ask.platform.api.dto.PlatformBusinessRowResponse;
import kz.ask.platform.domain.PlatformMembershipService;
import kz.ask.platform.domain.enums.ModerationStatus;
import kz.ask.platform.domain.enums.ModerationTargetType;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import kz.ask.shared.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PlatformBusinessesProcessor {

    private static final int MAX_PAGE_SIZE = 100;
    private static final String CREATED_AT_FIELD = "createdAt";

    private final PlatformMembershipService platformMembershipService;
    private final BusinessRepository businessRepository;
    private final BusinessBranchRepository businessBranchRepository;
    private final BusinessMemberRepository businessMemberRepository;
    private final ProductRepository productRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;
    private final UniqueOfferRepository uniqueOfferRepository;
    private final ModerationActionRepository moderationActionRepository;
    private final ItemMapper itemMapper;

    @Transactional(readOnly = true)
    public PlatformBusinessListResponse list(AskPrincipal principal, int page, int size, String query) {
        requirePlatformAccess(principal);
        Page<Business> businesses = businessRepository.searchForPlatform(
                term(query),
                PageRequest.of(
                        Math.max(page, 0),
                        Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                        Sort.by(Sort.Direction.DESC, CREATED_AT_FIELD)));
        return PlatformBusinessListResponse.builder()
                .items(businesses.getContent().stream().map(this::toRow).toList())
                .page(businesses.getNumber())
                .size(businesses.getSize())
                .totalElements(businesses.getTotalElements())
                .totalPages(businesses.getTotalPages())
                .build();
    }

    @Transactional(readOnly = true)
    public PlatformBusinessDetailResponse detail(AskPrincipal principal, UUID businessId) {
        requirePlatformAccess(principal);
        Business business = findBusiness(businessId);
        List<BusinessBranch> branches = businessBranchRepository.findByBusinessId(businessId);
        return PlatformBusinessDetailResponse.builder()
                .businessId(business.getId())
                .name(business.getName())
                .legalName(business.getLegalName())
                .bin(business.getBin())
                .countryCode(business.getCountryCode())
                .catalogStatus(resolveCatalogStatus(businessId))
                .businessScope(business.getScope().name())
                .branchCount((long) branches.size())
                .memberCount((long) businessMemberRepository.findByBusinessId(businessId).size())
                .productCount(productRepository.countByBusinessId(businessId))
                .serviceCount(serviceOfferingRepository.countByBusinessId(businessId))
                .dropCount(uniqueOfferRepository.countByBusinessIdAndStatusIn(
                        businessId, List.of(kz.ask.business.uniqueoffer.domain.enums.UniqueOfferStatus.ACTIVE)))
                .branches(branches.stream().map(this::toBranch).toList())
                .build();
    }

    @Transactional(readOnly = true)
    public BusinessProductListResponse listItems(
            AskPrincipal principal, UUID businessId, int page, int size) {
        requirePlatformAccess(principal);
        findBusiness(businessId);
        Page<Item> items = productRepository.findByBusinessId(
                businessId, pageRequest(page, size));
        return BusinessProductListResponse.builder()
                .items(items.getContent().stream()
                        .map(itemMapper::toProductOfferDto)
                        .map(itemMapper::toBusinessProductRowResponse)
                        .toList())
                .page(items.getNumber())
                .size(items.getSize())
                .totalElements(items.getTotalElements())
                .totalPages(items.getTotalPages())
                .build();
    }

    @Transactional(readOnly = true)
    public BusinessServiceListResponse listServices(
            AskPrincipal principal, UUID businessId, int page, int size) {
        requirePlatformAccess(principal);
        findBusiness(businessId);
        Page<Service> services = serviceOfferingRepository.findByBusinessId(
                businessId, pageRequest(page, size));
        return BusinessServiceListResponse.builder()
                .items(services.getContent().stream().map(this::toServiceRow).toList())
                .page(services.getNumber())
                .size(services.getSize())
                .totalElements(services.getTotalElements())
                .totalPages(services.getTotalPages())
                .build();
    }

    private PlatformBusinessRowResponse toRow(Business business) {
        UUID businessId = business.getId();
        List<BusinessMember> members = businessMemberRepository.findByBusinessId(businessId);
        String contactEmail = members.stream()
                .filter(member -> member.getRole() == Role.OWNER)
                .findFirst()
                .or(() -> members.stream().findFirst())
                .map(member -> member.getUser().getEmail())
                .orElse(null);
        return PlatformBusinessRowResponse.builder()
                .businessId(businessId)
                .name(business.getName())
                .legalName(business.getLegalName())
                .contactEmail(contactEmail)
                .branchCount((long) businessBranchRepository.findByBusinessId(businessId).size())
                .memberCount((long) members.size())
                .productCount(productRepository.countByBusinessId(businessId))
                .serviceCount(serviceOfferingRepository.countByBusinessId(businessId))
                .dropCount(uniqueOfferRepository.countByBusinessIdAndStatusIn(
                        businessId, List.of(kz.ask.business.uniqueoffer.domain.enums.UniqueOfferStatus.ACTIVE)))
                .catalogStatus(resolveCatalogStatus(businessId))
                .build();
    }

    private PlatformBusinessBranchResponse toBranch(BusinessBranch branch) {
        return PlatformBusinessBranchResponse.builder()
                .branchId(branch.getId())
                .name(branch.getName())
                .address(branch.getAddress())
                .build();
    }

    private BusinessServiceRowResponse toServiceRow(Service service) {
        return BusinessServiceRowResponse.builder()
                .serviceOfferingId(service.getId())
                .branchId(service.getBranch() == null ? null : service.getBranch().getId())
                .categoryId(service.getCategory().getId())
                .categoryLabel(service.getCategoryLabel())
                .name(service.getName())
                .description(service.getDescription())
                .serviceMode(service.getServiceMode())
                .basePrice(service.getBasePrice())
                .scheduleText(service.getScheduleText())
                .attributes(service.getAttributes())
                .isActive(service.getIsActive())
                .updatedAt(service.getUpdatedAt())
                .build();
    }

    private String resolveCatalogStatus(UUID businessId) {
        List<ModerationAction> actions =
                moderationActionRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc(
                        ModerationTargetType.BUSINESS, businessId);
        if (!actions.isEmpty()
                && "SOFT_DELETE".equals(actions.getFirst().getReasonCode())) {
            return "DELETED";
        }
        if (!actions.isEmpty()
                && actions.getFirst().getModerationStatus() == ModerationStatus.BANNED) {
            return "BLOCKED";
        }
        if (productRepository.existsByBusinessIdAndModerationStatus(
                businessId, ProductModerationStatus.PENDING)) {
            return "REVIEW_REQUIRED";
        }
        return "ACTIVE";
    }

    private Business findBusiness(UUID businessId) {
        return businessRepository.findById(businessId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.BUSINESS_NOT_FOUND, businessId));
    }

    private PageRequest pageRequest(int page, int size) {
        return PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, CREATED_AT_FIELD));
    }

    private String term(String query) {
        return query == null || query.isBlank() ? "" : "%" + query.trim().toLowerCase() + "%";
    }

    private void requirePlatformAccess(AskPrincipal principal) {
        if (platformMembershipService.findActiveByUser(principal.getUserId()) == null) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
    }
}
