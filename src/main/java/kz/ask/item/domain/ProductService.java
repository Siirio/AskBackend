package kz.ask.item.domain;

import java.util.UUID;
import kz.ask.item.api.dto.BusinessProductCreateRequest;
import kz.ask.item.api.dto.BusinessProductUpdateRequest;
import kz.ask.item.application.ProductOfferDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    Page<ProductOfferDto> listProducts(UUID branchId, Boolean enabled, String query, Pageable pageable);

    ProductOfferDto createProduct(UUID businessId, UUID branchId, BusinessProductCreateRequest req);

    ProductOfferDto updateProduct(UUID productId, UUID branchId, BusinessProductUpdateRequest req);

    ProductOfferDto deleteProduct(UUID productId, UUID branchId);
}
