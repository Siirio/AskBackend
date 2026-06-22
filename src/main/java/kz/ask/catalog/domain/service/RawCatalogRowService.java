package kz.ask.catalog.domain.service;

import java.util.List;
import java.util.UUID;
import kz.ask.catalog.domain.dto.RawCatalogRowDto;

public interface RawCatalogRowService {

    void saveAll(UUID catalogImportId, List<RawCatalogRowDto> rows);

    List<RawCatalogRowDto> findByCatalogImportId(UUID catalogImportId);

    void updateAfterValidation(UUID rowId, String status, String normalizedDataJson,
                               String validationErrorsJson, String validationWarningsJson);

    void updateAfterImport(UUID rowId, String status, UUID productId, UUID productOfferId);
}
