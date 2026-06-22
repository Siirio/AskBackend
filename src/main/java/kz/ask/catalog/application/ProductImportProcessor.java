package kz.ask.catalog.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import kz.ask.business.domain.BranchMemberService;
import kz.ask.business.domain.BusinessBranchService;
import kz.ask.business.domain.BusinessService;
import kz.ask.business.domain.dto.BusinessBranchDto;
import kz.ask.catalog.api.dto.ApproveResponse;
import kz.ask.catalog.api.dto.CancelResponse;
import kz.ask.catalog.api.dto.ColumnInfo;
import kz.ask.catalog.api.dto.MappingRequest;
import kz.ask.catalog.api.dto.PreviewResponse;
import kz.ask.catalog.api.dto.UploadResponse;
import kz.ask.catalog.domain.dto.CatalogImportColumnMappingDto;
import kz.ask.catalog.domain.dto.CatalogImportDto;
import kz.ask.catalog.domain.dto.RawCatalogRowDto;
import kz.ask.catalog.domain.enums.CatalogImportStatus;
import kz.ask.catalog.domain.service.CatalogImportColumnMappingService;
import kz.ask.catalog.domain.service.CatalogImportService;
import kz.ask.catalog.domain.service.ProductImportService;
import kz.ask.catalog.domain.service.RawCatalogRowService;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import kz.ask.shared.error.InternalServerException;
import kz.ask.shared.error.NotFoundException;
import kz.ask.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class ProductImportProcessor {

    private final BusinessService businessService;
    private final BusinessBranchService businessBranchService;
    private final BranchMemberService branchMemberService;
    private final CatalogImportService catalogImportService;
    private final CatalogImportColumnMappingService catalogImportColumnMappingService;
    private final RawCatalogRowService rawCatalogRowService;
    private final ProductImportService productImportService;
    private final ObjectMapper objectMapper;

    private BusinessBranchDto requireBranch(UUID branchId) {
        BusinessBranchDto branch = businessBranchService.findById(branchId);
        if (branch == null) {
            throw new NotFoundException(ErrorCode.BRANCH_NOT_FOUND);
        }
        return branch;
    }

    private CatalogImportDto requireImport(UUID branchId, UUID importId) {
        return catalogImportService.findByIdAndBranch(importId, branchId);
    }

    private void verifyBranchAccess(UUID userId, UUID branchId) {
        BusinessBranchDto branch = businessBranchService.findById(branchId);
        if (branch == null) {
            throw new NotFoundException(ErrorCode.BRANCH_NOT_FOUND);
        }
        if (businessService.isOwnerOfBusiness(branch.getBusinessId(), userId)) {
            return;
        }
        if (branchMemberService.isStaffOfBranch(branchId, userId)) {
            return;
        }
        throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
    }

    @Transactional
    public UploadResponse upload(AskPrincipal principal, UUID branchId, MultipartFile file) {
        verifyBranchAccess(principal.getUserId(), branchId);
        BusinessBranchDto branch = requireBranch(branchId);

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".xlsx")) {
            throw new ValidationException(ErrorCode.IMPORT_NOT_XLSX);
        }
        if (file.isEmpty()) {
            throw new ValidationException(ErrorCode.IMPORT_EMPTY_FILE);
        }

        try {
            CatalogImportDto catalogImport = productImportService.parseAndStore(
                file, branch.getBusinessId(), branchId, principal.getUserId());

            List<CatalogImportColumnMappingDto> mappings =
                    catalogImportColumnMappingService.findByCatalogImportId(catalogImport.getId());
            List<RawCatalogRowDto> rawRows = rawCatalogRowService.findByCatalogImportId(catalogImport.getId());
            List<RawCatalogRowDto> sampleRows = rawRows.size() > 3
                ? rawRows.subList(0, 3) : rawRows;

            return buildUploadResponse(catalogImport, mappings, sampleRows);
        } catch (IOException e) {
            throw new InternalServerException(ErrorCode.INTERNAL_ERROR);
        }
    }

    @Transactional
    public PreviewResponse mapColumns(AskPrincipal principal, UUID branchId, UUID importId,
                                       MappingRequest request) {
        verifyBranchAccess(principal.getUserId(), branchId);
        requireBranch(branchId);
        requireImport(branchId, importId);

        return productImportService.applyMappings(importId, request);
    }

    public PreviewResponse getPreview(AskPrincipal principal, UUID branchId, UUID importId) {
        verifyBranchAccess(principal.getUserId(), branchId);
        requireBranch(branchId);
        requireImport(branchId, importId);

        return productImportService.buildPreview(importId);
    }

    @Transactional
    public ApproveResponse approve(AskPrincipal principal, UUID branchId, UUID importId) {
        verifyBranchAccess(principal.getUserId(), branchId);
        BusinessBranchDto branch = requireBranch(branchId);
        CatalogImportDto catalogImport = requireImport(branchId, importId);

        if (!catalogImport.getStatus().equals(CatalogImportStatus.PREVIEW_READY.name())) {
            throw new ValidationException(ErrorCode.IMPORT_INVALID_STATUS);
        }

        Integer created = productImportService.approveImport(importId, branch.getBusinessId(), branchId);

        return buildApproveResponse(catalogImport, created);
    }

    @Transactional
    public CancelResponse cancel(AskPrincipal principal, UUID branchId, UUID importId) {
        verifyBranchAccess(principal.getUserId(), branchId);
        requireBranch(branchId);
        CatalogImportDto catalogImport = requireImport(branchId, importId);

        if (catalogImport.getStatus().equals(CatalogImportStatus.IMPORTED.name())) {
            throw new ValidationException(ErrorCode.IMPORT_INVALID_STATUS);
        }

        productImportService.cancelImport(importId);

        return buildCancelResponse(catalogImport);
    }

    private UploadResponse buildUploadResponse(CatalogImportDto catalogImport,
                                                List<CatalogImportColumnMappingDto> mappings,
                                                List<RawCatalogRowDto> sampleRows) {
        List<ColumnInfo> columns = mappings.stream()
            .map(m -> ColumnInfo.builder()
                .sourceColumn(m.getSourceColumn())
                .suggestedTargetField(m.getTargetField())
                .confidence(m.getConfidence())
                .build())
            .toList();

        List<Map<String, String>> sampleData = sampleRows.stream()
            .map(row -> {
                try {
                    return objectMapper.readValue(
                        row.getRowPayload(),
                        objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class));
                } catch (Exception e) {
                    return Map.<String, String>of();
                }
            })
            .toList();

        return UploadResponse.builder()
            .importId(catalogImport.getId())
            .originalFileName(catalogImport.getOriginalFileName())
            .status(catalogImport.getStatus())
            .totalRows(catalogImport.getTotalRows())
            .columns(columns)
            .sampleRows(sampleData)
            .build();
    }

    private ApproveResponse buildApproveResponse(CatalogImportDto catalogImport, Integer productsCreated) {
        return ApproveResponse.builder()
            .importId(catalogImport.getId())
            .status(catalogImport.getStatus())
            .productsCreated(productsCreated)
            .offersCreated(productsCreated)
            .rowsSkipped(catalogImport.getInvalidRows() != null ? catalogImport.getInvalidRows() : 0)
            .build();
    }

    private CancelResponse buildCancelResponse(CatalogImportDto catalogImport) {
        return CancelResponse.builder()
            .importId(catalogImport.getId())
            .status(catalogImport.getStatus())
            .build();
    }
}
