package kz.ask.catalog.domain;

import java.util.UUID;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.business.domain.entity.Category;
import kz.ask.business.infrastructure.repository.BusinessBranchRepository;
import kz.ask.business.infrastructure.repository.BusinessRepository;
import kz.ask.business.infrastructure.repository.CategoryRepository;
import kz.ask.catalog.api.dto.BusinessProductCreateRequest;
import kz.ask.catalog.api.dto.BusinessProductUpdateRequest;
import kz.ask.catalog.application.ProductOfferDto;
import kz.ask.catalog.domain.entity.Product;
import kz.ask.catalog.domain.entity.ProductOffer;
import kz.ask.catalog.infrastructure.mapper.ProductOfferMapper;
import kz.ask.catalog.infrastructure.repository.ProductOfferRepository;
import kz.ask.catalog.infrastructure.repository.ProductRepository;
import kz.ask.shared.error.ConflictException;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.NotFoundException;
import kz.ask.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductOfferRepository productOfferRepository;
    private final BusinessRepository businessRepository;
    private final BusinessBranchRepository businessBranchRepository;
    private final CategoryRepository categoryRepository;
    private final ProductOfferMapper productOfferMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductOfferDto> listOffers(UUID branchId, UUID categoryId, Boolean enabled, String query, Pageable pageable) {
        String term = (query == null || query.isBlank()) ? null : "%" + query.trim().toLowerCase() + "%";
        return productOfferRepository.search(branchId, categoryId, enabled, term, pageable)
                .map(productOfferMapper::toDto);
    }

    @Override
    @Transactional
    public ProductOfferDto createProduct(UUID businessId, UUID branchId, BusinessProductCreateRequest req) {
        String normalizedSku = blankToNull(req.getSku());
        if (normalizedSku != null && productRepository.existsByBusinessIdAndSkuIgnoreCase(businessId, normalizedSku)) {
            throw new ConflictException(ErrorCode.SKU_ALREADY_EXISTS);
        }

        Business businessRef = businessRepository.getReferenceById(businessId);
        BusinessBranch branchRef = businessBranchRepository.getReferenceById(branchId);
        Category categoryRef = categoryRepository.getReferenceById(req.getCategoryId());

        Product product = productRepository.save(productOfferMapper.toProductEntity(req, businessRef, categoryRef));
        ProductOffer offer = productOfferRepository.save(productOfferMapper.toOfferEntity(req, product, branchRef));
        return productOfferMapper.toDto(offer);
    }

    @Override
    @Transactional
    public ProductOfferDto updateProduct(UUID productId, UUID branchId, BusinessProductUpdateRequest req) {
        ProductOffer offer = findOfferOrThrow(productId, branchId);
        Product product = offer.getProduct();

        if (req.getName() != null && req.getName().isBlank()) {
            throw new ValidationException(ErrorCode.PRODUCT_NAME_BLANK);
        }
        String normalizedSku = blankToNull(req.getSku());
        if (normalizedSku != null
                && productRepository.existsByBusinessIdAndSkuIgnoreCaseAndIdNot(
                        product.getBusiness().getId(), normalizedSku, product.getId())) {
            throw new ConflictException(ErrorCode.SKU_ALREADY_EXISTS);
        }

        Category categoryRef = req.getCategoryId() != null
                ? categoryRepository.getReferenceById(req.getCategoryId())
                : null;
        productOfferMapper.applyUpdate(req, product, offer, categoryRef);
        offer.setSearchVersion(offer.getSearchVersion() + 1);
        return productOfferMapper.toDto(offer);
    }

    @Override
    @Transactional
    public ProductOfferDto deleteProduct(UUID productId, UUID branchId) {
        ProductOffer offer = findOfferOrThrow(productId, branchId);
        productOfferMapper.markArchived(offer.getProduct(), offer);
        offer.setSearchVersion(offer.getSearchVersion() + 1);
        return productOfferMapper.toDto(offer);
    }

    private ProductOffer findOfferOrThrow(UUID productId, UUID branchId) {
        return productOfferRepository.findByProductIdAndBranchId(productId, branchId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
