package kz.ask.catalog.domain.service;

import kz.ask.catalog.domain.dto.CreateProductDto;
import kz.ask.catalog.domain.dto.ProductDto;

public interface ProductService {
    ProductDto create(CreateProductDto dto);
}
