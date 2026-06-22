package kz.ask.catalog.domain.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import kz.ask.catalog.domain.dto.RawCatalogRowDto;
import kz.ask.catalog.domain.entity.CatalogImport;
import kz.ask.catalog.domain.entity.RawCatalogRow;
import kz.ask.catalog.domain.enums.RawRowStatus;
import kz.ask.catalog.infrastructure.mapper.CatalogImportMapper;
import kz.ask.catalog.infrastructure.repository.CatalogImportRepository;
import kz.ask.catalog.infrastructure.repository.ProductOfferRepository;
import kz.ask.catalog.infrastructure.repository.ProductRepository;
import kz.ask.catalog.infrastructure.repository.RawCatalogRowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RawCatalogRowServiceImpl implements RawCatalogRowService {

    private final RawCatalogRowRepository rawRowRepository;
    private final CatalogImportRepository catalogImportRepository;
    private final ProductRepository productRepository;
    private final ProductOfferRepository productOfferRepository;
    private final CatalogImportMapper mapper;

    @Override
    @Transactional
    public void saveAll(UUID catalogImportId, List<RawCatalogRowDto> dtos) {
        CatalogImport catalogImport = catalogImportRepository.getReferenceById(catalogImportId);
        List<RawCatalogRow> entities = dtos.stream()
                .map(dto -> mapper.toRawCatalogRowEntity(dto, catalogImport))
                .collect(Collectors.toList());
        rawRowRepository.saveAll(entities);
    }

    @Override
    public List<RawCatalogRowDto> findByCatalogImportId(UUID catalogImportId) {
        List<RawCatalogRow> entities = rawRowRepository.findByCatalogImportIdOrderByRowNumberAsc(catalogImportId);
        return mapper.toRawCatalogRowDtoList(entities);
    }

    @Override
    @Transactional
    public void updateAfterValidation(UUID rowId, String status, String normalizedDataJson,
                                       String validationErrorsJson, String validationWarningsJson) {
        RawCatalogRow row = rawRowRepository.getReferenceById(rowId);
        row.setStatus(RawRowStatus.valueOf(status));
        row.setNormalizedDataJson(normalizedDataJson);
        row.setValidationErrorsJson(validationErrorsJson);
        row.setValidationWarningsJson(validationWarningsJson);
        rawRowRepository.save(row);
    }

    @Override
    @Transactional
    public void updateAfterImport(UUID rowId, String status, UUID productId, UUID productOfferId) {
        RawCatalogRow row = rawRowRepository.getReferenceById(rowId);
        row.setStatus(RawRowStatus.valueOf(status));
        row.setProduct(productRepository.getReferenceById(productId));
        row.setProductOffer(productOfferRepository.getReferenceById(productOfferId));
        rawRowRepository.save(row);
    }
}
