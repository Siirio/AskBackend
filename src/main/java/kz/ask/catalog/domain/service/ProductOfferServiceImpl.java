package kz.ask.catalog.domain.service;

import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.business.infrastructure.repository.BusinessBranchRepository;
import kz.ask.catalog.domain.dto.CreateProductOfferDto;
import kz.ask.catalog.domain.dto.ProductOfferDto;
import kz.ask.catalog.domain.entity.Product;
import kz.ask.catalog.domain.entity.ProductOffer;
import kz.ask.catalog.infrastructure.mapper.CatalogImportMapper;
import kz.ask.catalog.infrastructure.repository.ProductOfferRepository;
import kz.ask.catalog.infrastructure.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductOfferServiceImpl implements ProductOfferService {

    private final ProductOfferRepository productOfferRepository;
    private final ProductRepository productRepository;
    private final BusinessBranchRepository businessBranchRepository;
    private final CatalogImportMapper mapper;

    @Override
    @Transactional
    public ProductOfferDto create(CreateProductOfferDto dto) {
        Product productRef = productRepository.getReferenceById(dto.getProductId());
        BusinessBranch branchRef = businessBranchRepository.getReferenceById(dto.getBranchId());
        ProductOffer offer = mapper.toProductOfferEntity(productRef, branchRef, dto.getPrice());
        ProductOffer saved = productOfferRepository.save(offer);
        return mapper.toProductOfferDto(saved);
    }

}
