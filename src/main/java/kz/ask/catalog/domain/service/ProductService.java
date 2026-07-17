package kz.ask.catalog.domain.service;

import java.util.UUID;
import kz.ask.catalog.domain.dto.CreateProductDto;
import kz.ask.catalog.domain.dto.ProductDto;

public interface ProductService {
    ProductDto create(CreateProductDto dto);

    ProductDto setHiddenByModerator(UUID productId, Boolean hidden);
}
