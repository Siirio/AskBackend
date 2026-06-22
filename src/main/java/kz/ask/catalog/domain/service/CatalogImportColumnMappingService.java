package kz.ask.catalog.domain.service;

import java.util.List;
import java.util.UUID;
import kz.ask.catalog.domain.dto.CatalogImportColumnMappingDto;

public interface CatalogImportColumnMappingService {

    void saveAll(UUID catalogImportId, List<CatalogImportColumnMappingDto> mappings);

    List<CatalogImportColumnMappingDto> findByCatalogImportId(UUID catalogImportId);

    void deleteByCatalogImportId(UUID catalogImportId);
}
