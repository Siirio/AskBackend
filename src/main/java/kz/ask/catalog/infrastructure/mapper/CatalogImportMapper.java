package kz.ask.catalog.infrastructure.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.catalog.api.dto.ColumnMappingInfo;
import kz.ask.catalog.api.dto.PreviewResponse;
import kz.ask.catalog.api.dto.RowPreview;
import kz.ask.catalog.domain.dto.CatalogImportColumnMappingDto;
import kz.ask.catalog.domain.dto.CatalogImportDto;
import kz.ask.catalog.domain.dto.CreateProductDto;
import kz.ask.catalog.domain.dto.CreateProductOfferDto;
import kz.ask.catalog.domain.dto.ProductDto;
import kz.ask.catalog.domain.dto.ProductOfferDto;
import kz.ask.catalog.domain.dto.RawCatalogRowDto;
import kz.ask.catalog.domain.entity.CatalogImport;
import kz.ask.catalog.domain.entity.CatalogImportColumnMapping;
import kz.ask.catalog.domain.entity.Product;
import kz.ask.catalog.domain.entity.ProductOffer;
import kz.ask.catalog.domain.entity.RawCatalogRow;
import kz.ask.catalog.domain.enums.RawRowStatus;
import kz.ask.catalog.domain.enums.TargetField;
import kz.ask.shared.domain.enums.RecordStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CatalogImportMapper {

    private final ObjectMapper objectMapper;

    public PreviewResponse toPreviewResponse(CatalogImportDto catalogImport,
                                              List<CatalogImportColumnMappingDto> mappings,
                                              List<RawCatalogRowDto> rows) {
        int valid = 0, invalid = 0, warning = 0;

        List<ColumnMappingInfo> mappingInfos = mappings.stream()
            .map(m -> {
                TargetField tf;
                try {
                    tf = TargetField.valueOf(m.getTargetField());
                } catch (IllegalArgumentException e) {
                    tf = TargetField.IGNORE;
                }
                return ColumnMappingInfo.builder()
                    .sourceColumn(m.getSourceColumn())
                    .targetField(tf)
                    .characteristicName(m.getCharacteristicName())
                    .approved(m.getApproved())
                    .confidence(m.getConfidence())
                    .build();
            })
            .collect(Collectors.toList());

        List<RowPreview> previewRows = new ArrayList<>();
        for (RawCatalogRowDto row : rows) {
            RawRowStatus status = row.getStatus() != null ? RawRowStatus.valueOf(row.getStatus()) : RawRowStatus.PENDING;
            if (status == RawRowStatus.VALID) valid++;
            else if (status == RawRowStatus.INVALID) invalid++;
            else if (status == RawRowStatus.WARNING) warning++;

            Map<String, String> normalizedData = parseNormalized(row.getNormalizedDataJson());
            List<String> errors = parseStringList(row.getValidationErrorsJson());
            List<String> warningsList = parseStringList(row.getValidationWarningsJson());

            previewRows.add(RowPreview.builder()
                .rowId(row.getId())
                .rowNumber(row.getRowNumber())
                .status(status.name())
                .normalizedData(normalizedData)
                .errors(errors)
                .warnings(warningsList)
                .build());
        }

        return PreviewResponse.builder()
            .importId(catalogImport.getId())
            .status(catalogImport.getStatus())
            .totalRows(rows.size())
            .validRows(valid)
            .invalidRows(invalid)
            .warningRows(warning)
            .mappings(mappingInfos)
            .rows(previewRows)
            .build();
    }

    public CreateProductDto toCreateProductDto(Map<String, String> normalized, UUID businessId) {
        List<String> tags = List.of();
        String tagsStr = normalized.get("TAGS");
        if (tagsStr != null && !tagsStr.isEmpty()) {
            tags = Arrays.asList(tagsStr.split(","));
        }

        Map<String, String> characteristics = new LinkedHashMap<>();
        for (var entry : normalized.entrySet()) {
            if (entry.getKey().startsWith("CHAR_")) {
                characteristics.put(entry.getKey().substring(5), entry.getValue());
            }
        }

        return CreateProductDto.builder()
            .businessId(businessId)
            .name(normalized.get("NAME"))
            .categoryLabel(normalized.get("CATEGORY_LABEL"))
            .description(buildDescription(normalized))
            .sku(normalized.get("SKU"))
            .tags(tags)
            .characteristics(characteristics)
            .build();
    }

    public CreateProductOfferDto toCreateProductOfferDto(UUID productId, UUID branchId,
                                                          Map<String, String> normalized) {
        BigDecimal price = null;
        String priceStr = normalized.get("PRICE");
        if (priceStr != null && !priceStr.isEmpty()) {
            try {
                price = new BigDecimal(priceStr.replace(",", ".").replace(" ", ""));
            } catch (NumberFormatException e) {
                price = null;
            }
        }

        return CreateProductOfferDto.builder()
            .productId(productId)
            .branchId(branchId)
            .price(price)
            .build();
    }

    public CatalogImportDto toCatalogImportDto(CatalogImport entity) {
        return CatalogImportDto.builder()
                .id(entity.getId())
                .originalFileName(entity.getOriginalFileName())
                .status(entity.getStatus().name())
                .totalRows(entity.getTotalRows())
                .validRows(entity.getValidRows())
                .invalidRows(entity.getInvalidRows())
                .warningRows(entity.getWarningRows())
                .importedAt(entity.getImportedAt())
                .build();
    }

    public CatalogImportColumnMappingDto toColumnMappingDto(CatalogImportColumnMapping entity) {
        return CatalogImportColumnMappingDto.builder()
                .id(entity.getId())
                .sourceColumn(entity.getSourceColumn())
                .targetField(entity.getTargetField())
                .characteristicName(entity.getCharacteristicName())
                .approved(entity.getApproved())
                .confidence(entity.getConfidence())
                .build();
    }

    public List<CatalogImportColumnMappingDto> toColumnMappingDtoList(List<CatalogImportColumnMapping> entities) {
        return entities.stream().map(this::toColumnMappingDto).collect(Collectors.toList());
    }

    public RawCatalogRowDto toRawCatalogRowDto(RawCatalogRow entity) {
        return RawCatalogRowDto.builder()
                .id(entity.getId())
                .rowNumber(entity.getRowNumber())
                .rowPayload(entity.getRowPayload())
                .normalizedDataJson(entity.getNormalizedDataJson())
                .validationErrorsJson(entity.getValidationErrorsJson())
                .validationWarningsJson(entity.getValidationWarningsJson())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .build();
    }

    public List<RawCatalogRowDto> toRawCatalogRowDtoList(List<RawCatalogRow> entities) {
        return entities.stream().map(this::toRawCatalogRowDto).collect(Collectors.toList());
    }

    public ProductDto toProductDto(Product product) {
        Map<String, String> characteristics = Map.of();
        try {
            characteristics = objectMapper.readValue(product.getCharacteristicsJson(),
                objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class));
        } catch (Exception e) {
            // leave empty map
        }

        return ProductDto.builder()
            .id(product.getId())
            .businessId(product.getBusiness() != null ? product.getBusiness().getId() : null)
            .categoryLabel(product.getCategoryLabel())
            .name(product.getName())
            .description(product.getDescription())
            .sku(product.getSku())
            .tags(product.getTags())
            .characteristics(characteristics)
            .status(product.getStatus() != null ? product.getStatus().name() : null)
            .build();
    }

    public ProductOfferDto toProductOfferDto(ProductOffer offer) {
        return ProductOfferDto.builder()
              .id(offer.getId())
              .searchVersion(offer.getSearchVersion())
            .productId(offer.getProduct() != null ? offer.getProduct().getId() : null)
            .branchId(offer.getBranch() != null ? offer.getBranch().getId() : null)
            .price(offer.getPrice())
            .enabled(offer.getEnabled())
            .status(offer.getStatus() != null ? offer.getStatus().name() : null)
            .build();
    }

    public Product toProductEntity(CreateProductDto dto, Business business) {
        Product product = new Product();
        product.setBusiness(business);
        product.setName(dto.getName());
        product.setCategoryLabel(dto.getCategoryLabel());
        product.setDescription(dto.getDescription());
        product.setSku(dto.getSku());
        product.setTags(dto.getTags());
        product.setCharacteristicsJson(characteristicsToJson(dto.getCharacteristics()));
        product.setStatus(RecordStatus.ACTIVE);
        return product;
    }

    public ProductOffer toProductOfferEntity(Product product, BusinessBranch branch, BigDecimal price) {
        ProductOffer offer = new ProductOffer();
        offer.setProduct(product);
        offer.setBranch(branch);
        offer.setPrice(price);
        offer.setEnabled(true);
        offer.setStatus(RecordStatus.ACTIVE);
        return offer;
    }

    public CatalogImportColumnMapping toColumnMappingEntity(CatalogImportColumnMappingDto dto,
                                                              CatalogImport catalogImport) {
        CatalogImportColumnMapping mapping = new CatalogImportColumnMapping();
        mapping.setCatalogImport(catalogImport);
        mapping.setSourceColumn(dto.getSourceColumn());
        mapping.setTargetField(dto.getTargetField());
        mapping.setCharacteristicName(dto.getCharacteristicName());
        mapping.setApproved(dto.getApproved());
        mapping.setConfidence(dto.getConfidence());
        return mapping;
    }

    public RawCatalogRow toRawCatalogRowEntity(RawCatalogRowDto dto, CatalogImport catalogImport) {
        RawCatalogRow row = new RawCatalogRow();
        row.setCatalogImport(catalogImport);
        row.setRowNumber(dto.getRowNumber());
        row.setRowPayload(dto.getRowPayload());
        row.setNormalizedDataJson(dto.getNormalizedDataJson());
        row.setValidationErrorsJson(dto.getValidationErrorsJson());
        row.setValidationWarningsJson(dto.getValidationWarningsJson());
        if (dto.getStatus() != null) {
            row.setStatus(RawRowStatus.valueOf(dto.getStatus()));
        }
        return row;
    }

    public String characteristicsToJson(Map<String, String> characteristics) {
        if (characteristics == null || characteristics.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(characteristics);
        } catch (Exception e) {
            return null;
        }
    }

    private String buildDescription(Map<String, String> normalized) {
        StringBuilder desc = new StringBuilder();
        String baseDesc = normalized.get("DESCRIPTION");
        if (baseDesc != null && !baseDesc.isEmpty()) {
            desc.append(baseDesc);
        }

        for (var entry : normalized.entrySet()) {
            if (entry.getKey().startsWith("APPEND_")) {
                String columnName = entry.getKey().substring(7);
                if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                    if (!desc.isEmpty()) {
                        desc.append("\n");
                    }
                    desc.append(columnName).append(": ").append(entry.getValue());
                }
            }
        }
        return desc.isEmpty() ? null : desc.toString();
    }

    private Map<String, String> parseNormalized(String json) {
        try {
            return objectMapper.readValue(json,
                objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class));
        } catch (Exception e) {
            return Map.of();
        }
    }

    private List<String> parseStringList(String json) {
        if (json == null) return List.of();
        try {
            return objectMapper.readValue(json,
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (Exception e) {
            return List.of();
        }
    }
}
