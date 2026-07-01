package kz.ask.catalog.domain.service;

import java.time.Instant;
import java.util.UUID;
import kz.ask.business.infrastructure.repository.BusinessBranchRepository;
import kz.ask.business.infrastructure.repository.BusinessRepository;
import kz.ask.business.infrastructure.repository.DataSourceRepository;
import kz.ask.catalog.domain.dto.CatalogImportDto;
import kz.ask.catalog.domain.entity.CatalogImport;
import kz.ask.catalog.domain.enums.CatalogImportStatus;
import kz.ask.catalog.infrastructure.mapper.CatalogImportMapper;
import kz.ask.catalog.infrastructure.repository.CatalogImportRepository;
import kz.ask.identity.infrastructure.repository.AppUserRepository;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CatalogImportServiceImpl implements CatalogImportService {

    private final CatalogImportRepository catalogImportRepository;
    private final BusinessRepository businessRepository;
    private final BusinessBranchRepository businessBranchRepository;
    private final DataSourceRepository dataSourceRepository;
    private final AppUserRepository appUserRepository;
    private final CatalogImportMapper mapper;

    @Override
    @Transactional
    public CatalogImportDto createImport(UUID dataSourceId, UUID businessId, UUID branchId,
                                           UUID createdBy, String originalFileName, Integer totalRows) {
        CatalogImport catalogImport = new CatalogImport();
        catalogImport.setDataSource(dataSourceRepository.getReferenceById(dataSourceId));
        catalogImport.setBusiness(businessRepository.getReferenceById(businessId));
        catalogImport.setBranch(businessBranchRepository.getReferenceById(branchId));
        catalogImport.setCreatedBy(appUserRepository.getReferenceById(createdBy));
        catalogImport.setOriginalFileName(originalFileName);
        catalogImport.setTotalRows(totalRows);
        catalogImport.setStatus(CatalogImportStatus.UPLOADED);
        CatalogImport saved = catalogImportRepository.save(catalogImport);
        return mapper.toCatalogImportDto(saved);
    }

    @Override
    public CatalogImportDto findById(UUID importId) {
        CatalogImport entity = catalogImportRepository.findById(importId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.IMPORT_NOT_FOUND));
        return mapper.toCatalogImportDto(entity);
    }

    @Override
    public CatalogImportDto findByIdAndBranch(UUID importId, UUID branchId) {
        CatalogImport entity = catalogImportRepository.findByIdAndBranchId(importId, branchId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.IMPORT_NOT_FOUND));
        return mapper.toCatalogImportDto(entity);
    }

    @Override
    @Transactional
    public void updateStatus(UUID catalogImportId, CatalogImportStatus status) {
        CatalogImport catalogImport = catalogImportRepository.getReferenceById(catalogImportId);
        catalogImport.setStatus(status);
        catalogImportRepository.save(catalogImport);
    }

    @Override
    @Transactional
    public void completeImport(UUID catalogImportId) {
        CatalogImport catalogImport = catalogImportRepository.getReferenceById(catalogImportId);
        catalogImport.setStatus(CatalogImportStatus.IMPORTED);
        catalogImport.setImportedAt(Instant.now());
        catalogImportRepository.save(catalogImport);
    }

    @Override
    @Transactional
    public void updateCounts(UUID catalogImportId, Integer valid, Integer invalid, Integer warning) {
        CatalogImport catalogImport = catalogImportRepository.getReferenceById(catalogImportId);
        catalogImport.setValidRows(valid);
        catalogImport.setInvalidRows(invalid);
        catalogImport.setWarningRows(warning);
        catalogImportRepository.save(catalogImport);
    }
}
