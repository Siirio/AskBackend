package kz.ask.catalog.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import kz.ask.business.domain.DataSourceService;
import kz.ask.business.domain.dto.DataSourceDto;
import kz.ask.business.domain.enums.DataSourceType;
import kz.ask.catalog.api.dto.MappingRequest;
import kz.ask.catalog.api.dto.PreviewResponse;
import kz.ask.catalog.domain.dto.CatalogImportColumnMappingDto;
import kz.ask.catalog.domain.dto.CatalogImportDto;
import kz.ask.catalog.domain.dto.CreateProductDto;
import kz.ask.catalog.domain.dto.CreateProductOfferDto;
import kz.ask.catalog.domain.dto.ProductDto;
import kz.ask.catalog.domain.dto.ProductOfferDto;
import kz.ask.catalog.domain.dto.RawCatalogRowDto;
import kz.ask.catalog.domain.entity.RawCatalogRow;
import kz.ask.catalog.domain.enums.CatalogImportStatus;
import kz.ask.catalog.domain.enums.RawRowStatus;
import kz.ask.catalog.infrastructure.mapper.CatalogImportMapper;
import kz.ask.catalog.infrastructure.repository.RawCatalogRowRepository;
import kz.ask.search.domain.SearchService;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProductImportServiceImpl implements ProductImportService {

    private final ExcelParser excelParser;
    private final AutoMappingEngine autoMappingEngine;
    private final RowNormalizer rowNormalizer;
    private final CatalogImportService catalogImportService;
    private final CatalogImportColumnMappingService catalogImportColumnMappingService;
    private final RawCatalogRowService rawCatalogRowService;
    private final DataSourceService dataSourceService;
    private final ProductService productService;
    private final ProductOfferService productOfferService;
    private final RawCatalogRowRepository rawCatalogRowRepository;
    private final SearchService searchService;
    private final CatalogImportMapper mapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public CatalogImportDto parseAndStore(MultipartFile file, UUID businessId, UUID branchId,
                                          UUID createdBy) throws IOException {
        String originalFilename = file.getOriginalFilename() != null
            ? file.getOriginalFilename() : "import.xlsx";

        ExcelParseResult result = excelParser.parse(file.getInputStream());

        if (result.columns().isEmpty()) {
            throw new ValidationException(ErrorCode.IMPORT_EMPTY_FILE);
        }

        DataSourceDto dataSourceDto = dataSourceService.resolve(businessId, DataSourceType.EXCEL);

        CatalogImportDto catalogImportDto = catalogImportService.createImport(
            dataSourceDto.getId(), businessId, branchId, createdBy,
            originalFilename, result.rows().size());
        UUID importId = catalogImportDto.getId();

        List<CatalogImportColumnMappingDto> autoMappingDtos = new ArrayList<>();
        for (String column : result.columns()) {
            MappingSuggestion suggestion = autoMappingEngine.suggest(column);
            autoMappingDtos.add(CatalogImportColumnMappingDto.builder()
                    .sourceColumn(column)
                    .targetField(suggestion.targetField().name())
                    .confidence(suggestion.confidence())
                    .approved(false)
                    .build());
        }
        catalogImportColumnMappingService.saveAll(importId, autoMappingDtos);

        List<RawCatalogRowDto> rawRowDtos = new ArrayList<>();
        for (int i = 0; i < result.rows().size(); i++) {
            String rowJson = objectMapper.writeValueAsString(result.rows().get(i));
            rawRowDtos.add(RawCatalogRowDto.builder()
                    .rowNumber(i + 1)
                    .rowPayload(rowJson)
                    .status(RawRowStatus.PENDING.name())
                    .build());
        }
        rawCatalogRowService.saveAll(importId, rawRowDtos);

        catalogImportService.updateStatus(importId, CatalogImportStatus.MAPPING_REQUIRED);

        return catalogImportDto;
    }

    @Override
    @Transactional
    public PreviewResponse applyMappings(UUID catalogImportId, MappingRequest request) {
        List<CatalogImportColumnMappingDto> mappingDtos = request.getMappings().stream()
            .map(entry -> CatalogImportColumnMappingDto.builder()
                    .sourceColumn(entry.getSourceColumn())
                    .targetField(entry.getTargetField().name())
                    .characteristicName(entry.getCharacteristicName())
                    .approved(true)
                    .confidence(1.0)
                    .build())
            .collect(Collectors.toList());

        catalogImportColumnMappingService.saveAll(catalogImportId, mappingDtos);

        List<RawCatalogRowDto> rowDtos = rawCatalogRowService.findByCatalogImportId(catalogImportId);
        for (RawCatalogRowDto rowDto : rowDtos) {
            RawCatalogRow row = rawCatalogRowRepository.getReferenceById(rowDto.getId());
            rowNormalizer.normalize(row, mappingDtos);
            rawCatalogRowService.updateAfterValidation(row.getId(), row.getStatus().name(),
                    row.getNormalizedDataJson(), row.getValidationErrorsJson(),
                    row.getValidationWarningsJson());
        }

        catalogImportService.updateStatus(catalogImportId, CatalogImportStatus.PREVIEW_READY);

        return buildPreview(catalogImportId);
    }

    @Override
    public PreviewResponse buildPreview(UUID catalogImportId) {
        CatalogImportDto catalogImportDto = catalogImportService.findById(catalogImportId);
        List<CatalogImportColumnMappingDto> mappingDtos =
                catalogImportColumnMappingService.findByCatalogImportId(catalogImportId);
        List<RawCatalogRowDto> rowDtos = rawCatalogRowService.findByCatalogImportId(catalogImportId);

        PreviewResponse response = mapper.toPreviewResponse(catalogImportDto, mappingDtos, rowDtos);

        catalogImportService.updateCounts(catalogImportId,
            response.getValidRows(), response.getInvalidRows(), response.getWarningRows());

        return response;
    }

    @Override
    @Transactional
    public Integer approveImport(UUID catalogImportId, UUID businessId, UUID branchId) {
        List<RawCatalogRowDto> rowDtos = rawCatalogRowService.findByCatalogImportId(catalogImportId);

        int created = 0;

        for (RawCatalogRowDto rowDto : rowDtos) {
            if ("INVALID".equals(rowDto.getStatus()) || "PENDING".equals(rowDto.getStatus())) {
                continue;
            }

            Map<String, String> normalized = rowNormalizer.parseNormalized(rowDto.getNormalizedDataJson());
            if (!normalized.containsKey("NAME")) {
                continue;
            }

            CreateProductDto productDto = mapper.toCreateProductDto(normalized, businessId);
            ProductDto savedProduct = productService.create(productDto);

            CreateProductOfferDto offerDto = mapper.toCreateProductOfferDto(
                savedProduct.getId(), branchId, normalized);
            ProductOfferDto savedOffer = productOfferService.create(offerDto);

            rawCatalogRowService.updateAfterImport(rowDto.getId(), RawRowStatus.VALID.name(),
                    savedProduct.getId(), savedOffer.getId());

            searchService.indexProductOffer(savedOffer.getId(), savedProduct.getId(),
                    businessId, branchId);

            created++;
        }

        catalogImportService.completeImport(catalogImportId);

        return created;
    }

    @Override
    @Transactional
    public void cancelImport(UUID catalogImportId) {
        catalogImportService.updateStatus(catalogImportId, CatalogImportStatus.CANCELLED);
    }
}
