package kz.ask.catalog.domain.service;

import kz.ask.catalog.domain.dto.CreateProductOfferDto;
import kz.ask.catalog.domain.dto.ProductOfferDto;

public interface ProductOfferService {

    ProductOfferDto create(CreateProductOfferDto dto);
}
