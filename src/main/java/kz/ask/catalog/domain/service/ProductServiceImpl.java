package kz.ask.catalog.domain.service;

import kz.ask.business.domain.entity.Business;
import kz.ask.business.infrastructure.repository.BusinessRepository;
import kz.ask.catalog.domain.dto.CreateProductDto;
import kz.ask.catalog.domain.dto.ProductDto;
import kz.ask.catalog.domain.entity.Product;
import kz.ask.catalog.infrastructure.mapper.CatalogImportMapper;
import kz.ask.catalog.infrastructure.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("catalogImportProductService")
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final BusinessRepository businessRepository;
    private final CatalogImportMapper mapper;

    @Override
    @Transactional
    public ProductDto create(CreateProductDto dto) {
        Business businessRef = businessRepository.getReferenceById(dto.getBusinessId());
        Product product = mapper.toProductEntity(dto, businessRef);
        Product saved = productRepository.save(product);
        return mapper.toProductDto(saved);
    }

}
