package kz.ask.platform.application;

import java.util.List;
import java.util.UUID;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.business.domain.enums.UniqueOfferStatus;
import kz.ask.business.infrastructure.repository.BusinessBranchRepository;
import kz.ask.business.infrastructure.repository.BusinessMemberRepository;
import kz.ask.business.infrastructure.repository.BusinessRepository;
import kz.ask.business.infrastructure.repository.UniqueOfferRepository;
import kz.ask.catalog.api.dto.BusinessProductCreateRequest;
import kz.ask.catalog.application.BusinessProductProcessor;
import kz.ask.catalog.application.ProductOfferDto;
import kz.ask.catalog.domain.ProductService;
import kz.ask.catalog.domain.entity.Product;
import kz.ask.catalog.infrastructure.repository.ProductRepository;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.platform.api.dto.PlatformBusinessDetailResponse;
import kz.ask.platform.api.dto.PlatformBusinessListResponse;
import kz.ask.platform.api.dto.PlatformBusinessRowResponse;
import kz.ask.platform.api.dto.PlatformProductCreateRequest;
import kz.ask.platform.domain.PlatformMembershipService;
import kz.ask.platform.domain.dto.PlatformMembershipDto;
import kz.ask.service.domain.entity.ServiceOffering;
import kz.ask.service.infrastructure.repository.ServiceOfferingRepository;
import kz.ask.shared.domain.enums.RecordStatus;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import kz.ask.shared.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PlatformBusinessProcessor {

    private static final int MAX_PAGE_SIZE = 100;
    private static final List<UniqueOfferStatus> ACTIVE_OFFER_STATUSES =
            List.of(UniqueOfferStatus.ACTIVE, UniqueOfferStatus.UPCOMING);

    private final BusinessRepository businessRepository;
    private final BusinessBranchRepository businessBranchRepository;
    private final BusinessMemberRepository businessMemberRepository;
    private final ProductRepository productRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;
    private final UniqueOfferRepository uniqueOfferRepository;
    private final ProductService productService;
    private final PlatformMembershipService platformMembershipService;

    @Transactional(readOnly = true)
    public PlatformBusinessListResponse listBusinesses(AskPrincipal principal, int page, int size, String query) {
        requirePlatformMembership(principal);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);
        Page<Business> businessPage = businessRepository.findAll(PageRequest.of(safePage, safeSize));
        List<PlatformBusinessRowResponse> items = businessPage.getContent().stream()
                .map(this::toRowResponse)
                .toList();
        return PlatformBusinessListResponse.builder()
                .items(items)
                .page(businessPage.getNumber())
                .size(businessPage.getSize())
                .totalElements(businessPage.getTotalElements())
                .totalPages(businessPage.getTotalPages())
                .build();
    }

    @Transactional(readOnly = true)
    public PlatformBusinessDetailResponse detail(AskPrincipal principal, UUID businessId) {
        requirePlatformMembership(principal);
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.BUSINESS_NOT_FOUND, businessId));
        return toDetailResponse(business);
    }

    @Transactional(readOnly = true)
    public Page<Product> listProducts(AskPrincipal principal, UUID businessId, int page, int size) {
        requirePlatformMembership(principal);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);
        return productRepository.findByBusinessId(businessId, PageRequest.of(safePage, safeSize));
    }

    @Transactional(readOnly = true)
    public Page<ServiceOffering> listServices(AskPrincipal principal, UUID businessId, int page, int size) {
        requirePlatformMembership(principal);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);
        return serviceOfferingRepository.findByBusinessId(businessId, PageRequest.of(safePage, safeSize));
    }

    @Transactional
    public ProductOfferDto createProduct(AskPrincipal principal, UUID businessId, PlatformProductCreateRequest request) {
        requirePlatformMembership(principal);
        BusinessBranch defaultBranch = findDefaultBranch(businessId);
        BusinessProductCreateRequest createRequest = BusinessProductCreateRequest.builder()
                .name(request.getName())
                .categoryLabel(request.getCategoryLabel())
                .description(request.getDescription())
                .sku(request.getSku())
                .tags(request.getTags())
                .build();
        return productService.createProduct(businessId, defaultBranch.getId(), createRequest);
    }

    @Transactional
    public void deleteProduct(AskPrincipal principal, UUID productId) {
        requirePlatformMembership(principal);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, productId));
        product.setStatus(RecordStatus.ARCHIVED);
        productRepository.save(product);
    }

    private BusinessBranch findDefaultBranch(UUID businessId) {
        List<BusinessBranch> branches = businessBranchRepository.findByBusinessIdAndStatus(
                businessId, RecordStatus.ACTIVE);
        if (branches.isEmpty()) {
            throw new NotFoundException(ErrorCode.BRANCH_NOT_FOUND);
        }
        return branches.get(0);
    }

    private PlatformBusinessRowResponse toRowResponse(Business business) {
        UUID businessId = business.getId();
        return PlatformBusinessRowResponse.builder()
                .businessId(businessId)
                .name(business.getName())
                .legalName(business.getLegalName())
                .contactEmail(business.getPreferredContactValue())
                .branchCount(businessBranchRepository.findByBusinessIdAndStatus(businessId, RecordStatus.ACTIVE).size())
                .memberCount(businessMemberRepository.findByBusinessIdAndStatus(businessId, RecordStatus.ACTIVE).size())
                .productCount(productRepository.countByBusinessIdAndStatus(businessId, RecordStatus.ACTIVE))
                .serviceCount(serviceOfferingRepository.countByBusinessIdAndStatus(businessId, RecordStatus.ACTIVE))
                .dropCount(uniqueOfferRepository.countByBusinessIdAndStatusIn(businessId, ACTIVE_OFFER_STATUSES))
                .moderationStatus(business.getModerationStatus().name())
                .catalogStatus(business.getCatalogStatus() != null ? business.getCatalogStatus().name() : null)
                .build();
    }

    private PlatformBusinessDetailResponse toDetailResponse(Business business) {
        UUID businessId = business.getId();
        List<BusinessBranch> branches = businessBranchRepository.findByBusinessIdAndStatus(
                businessId, RecordStatus.ACTIVE);
        List<PlatformBusinessDetailResponse.BusinessBranchDto> branchDtos = branches.stream()
                .map(branch -> PlatformBusinessDetailResponse.BusinessBranchDto.builder()
                        .branchId(branch.getId())
                        .name(branch.getName())
                        .address(branch.getAddress())
                        .onlineOnly(branch.getOnlineOnly())
                        .build())
                .toList();
        return PlatformBusinessDetailResponse.builder()
                .businessId(businessId)
                .name(business.getName())
                .legalName(business.getLegalName())
                .bin(business.getBin())
                .countryCode(business.getCountryCode())
                .preferredContactChannel(business.getPreferredContactChannel() != null
                        ? business.getPreferredContactChannel().name() : null)
                .preferredContactValue(business.getPreferredContactValue())
                .moderationStatus(business.getModerationStatus().name())
                .catalogStatus(business.getCatalogStatus() != null ? business.getCatalogStatus().name() : null)
                .catalogScope(business.getCatalogScope() != null ? business.getCatalogScope().name() : null)
                .branchCount(branches.size())
                .memberCount(businessMemberRepository.findByBusinessIdAndStatus(businessId, RecordStatus.ACTIVE).size())
                .productCount(productRepository.countByBusinessIdAndStatus(businessId, RecordStatus.ACTIVE))
                .serviceCount(serviceOfferingRepository.countByBusinessIdAndStatus(businessId, RecordStatus.ACTIVE))
                .dropCount(uniqueOfferRepository.countByBusinessIdAndStatusIn(businessId, ACTIVE_OFFER_STATUSES))
                .branches(branchDtos)
                .build();
    }

    private void requirePlatformMembership(AskPrincipal principal) {
        PlatformMembershipDto membership = platformMembershipService.findActiveByUser(principal.getUserId());
        if (membership == null) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
    }
}
