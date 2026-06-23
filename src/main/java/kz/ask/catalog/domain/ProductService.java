package kz.ask.catalog.domain;

import java.util.UUID;
import kz.ask.catalog.api.dto.BusinessProductCreateRequest;
import kz.ask.catalog.api.dto.BusinessProductUpdateRequest;
import kz.ask.catalog.application.ProductOfferDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    Page<ProductOfferDto> listOffers(UUID branchId, UUID categoryId, Boolean enabled, String query, Pageable pageable);

    ProductOfferDto createProduct(UUID businessId, UUID branchId, BusinessProductCreateRequest req);

    ProductOfferDto updateProduct(UUID productId, UUID branchId, BusinessProductUpdateRequest req);

    ProductOfferDto deleteProduct(UUID productId, UUID branchId);
}
