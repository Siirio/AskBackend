package kz.ask.catalog.domain.service;

import java.util.UUID;
import kz.ask.catalog.domain.dto.CatalogImportDto;
import kz.ask.catalog.domain.enums.CatalogImportStatus;

public interface CatalogImportService {

    CatalogImportDto createImport(UUID dataSourceId, UUID businessId, UUID branchId,
                                   UUID createdBy, String originalFileName, Integer totalRows);

    CatalogImportDto findById(UUID importId);

    CatalogImportDto findByIdAndBranch(UUID importId, UUID branchId);

    void updateStatus(UUID catalogImportId, CatalogImportStatus status);

    void completeImport(UUID catalogImportId);

    void updateCounts(UUID catalogImportId, Integer valid, Integer invalid, Integer warning);
}
