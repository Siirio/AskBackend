package kz.ask.catalog.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import kz.ask.catalog.domain.dto.CatalogImportColumnMappingDto;
import kz.ask.catalog.domain.entity.RawCatalogRow;
import kz.ask.catalog.domain.enums.RawRowStatus;
import kz.ask.catalog.domain.enums.TargetField;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RowNormalizer {

    private final ObjectMapper objectMapper;

    public void normalize(RawCatalogRow rawRow, List<CatalogImportColumnMappingDto> mappings) {
        Map<String, String> rawData;
        try {
            rawData = objectMapper.readValue(rawRow.getRowPayload(),
                objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class));
        } catch (Exception e) {
            rawRow.setNormalizedDataJson("{}");
            rawRow.setValidationErrorsJson("[\"Failed to parse row data\"]");
            rawRow.setValidationWarningsJson("[]");
            rawRow.setStatus(RawRowStatus.INVALID);
            return;
        }

        Map<String, String> normalized = new LinkedHashMap<>();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        for (CatalogImportColumnMappingDto mapping : mappings) {
            String sourceColumn = mapping.getSourceColumn();
            String targetFieldStr = mapping.getTargetField();
            TargetField targetField;
            try {
                targetField = TargetField.valueOf(targetFieldStr);
            } catch (IllegalArgumentException e) {
                continue;
            }

            String value = rawData.getOrDefault(sourceColumn, "").trim();

            switch (targetField) {
                case NAME:
                    if (value.isEmpty()) {
                        errors.add("Название товара обязательно");
                    } else {
                        normalized.put("NAME", value);
                    }
                    break;
                case CATEGORY_LABEL:
                    if (!value.isEmpty()) {
                        normalized.put("CATEGORY_LABEL", value);
                    }
                    break;
                case DESCRIPTION:
                    if (!value.isEmpty()) {
                        normalized.put("DESCRIPTION", value);
                    }
                    break;
                case SKU:
                    if (!value.isEmpty()) {
                        normalized.put("SKU", value);
                    }
                    break;
                case PRICE:
                    if (!value.isEmpty()) {
                        try {
                            new BigDecimal(value.replace(",", ".").replace(" ", ""));
                            normalized.put("PRICE", value);
                        } catch (NumberFormatException e) {
                            warnings.add("Цена не является числом: " + value);
                        }
                    }
                    break;
                case TAGS:
                    if (!value.isEmpty()) {
                        String[] tags = value.split("[,;]");
                        List<String> cleanTags = Arrays.stream(tags)
                            .map(String::trim)
                            .filter(t -> !t.isEmpty())
                            .toList();
                        if (!cleanTags.isEmpty()) {
                            normalized.put("TAGS", String.join(",", cleanTags));
                        }
                    }
                    break;
                case CHARACTERISTIC:
                    if (!value.isEmpty()) {
                        String name = mapping.getCharacteristicName() != null
                            ? mapping.getCharacteristicName() : sourceColumn;
                        normalized.put("CHAR_" + name, value);
                    }
                    break;
                case APPEND_TO_DESCRIPTION:
                    if (!value.isEmpty()) {
                        normalized.put("APPEND_" + sourceColumn, value);
                    }
                    break;
                case IGNORE:
                default:
                    break;
            }
        }

        boolean hasName = normalized.containsKey("NAME");

        try {
            rawRow.setNormalizedDataJson(objectMapper.writeValueAsString(normalized));
        } catch (Exception e) {
            rawRow.setNormalizedDataJson("{}");
        }

        try {
            rawRow.setValidationErrorsJson(objectMapper.writeValueAsString(errors));
        } catch (Exception e) {
            rawRow.setValidationErrorsJson("[]");
        }

        try {
            rawRow.setValidationWarningsJson(objectMapper.writeValueAsString(warnings));
        } catch (Exception e) {
            rawRow.setValidationWarningsJson("[]");
        }

        if (!hasName) {
            rawRow.setStatus(RawRowStatus.INVALID);
        } else if (!warnings.isEmpty()) {
            rawRow.setStatus(RawRowStatus.WARNING);
        } else {
            rawRow.setStatus(RawRowStatus.VALID);
        }
    }

    public Map<String, String> parseNormalized(String json) {
        try {
            return objectMapper.readValue(json,
                objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class));
        } catch (Exception e) {
            return Map.of();
        }
    }
}
