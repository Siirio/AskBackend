package kz.ask.catalog.domain.service;

import java.io.IOException;
import java.util.UUID;
import kz.ask.catalog.api.dto.MappingRequest;
import kz.ask.catalog.api.dto.PreviewResponse;
import kz.ask.catalog.domain.dto.CatalogImportDto;
import org.springframework.web.multipart.MultipartFile;

public interface ProductImportService {

    CatalogImportDto parseAndStore(MultipartFile file, UUID businessId, UUID branchId, UUID createdBy)
        throws IOException;

    PreviewResponse applyMappings(UUID catalogImportId, MappingRequest request);

    PreviewResponse buildPreview(UUID catalogImportId);

    Integer approveImport(UUID catalogImportId, UUID businessId, UUID branchId);

    void cancelImport(UUID catalogImportId);
}
