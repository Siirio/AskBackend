package kz.ask.importing.application;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kz.ask.importing.api.dto.ItemImportColumnInfo;
import kz.ask.importing.api.dto.ItemImportTargetField;
import org.springframework.stereotype.Component;

@Component
public class ItemImportAutoMapper {

    private static final Set<String> IGNORED_COLUMNS = Set.of(
            "остаток", "наличие", "количество", "склад",
            "stock", "quantity", "warehouse", "available", "availability");

    private static final Map<ItemImportTargetField, List<String>> PATTERNS = patterns();

    public ItemImportColumnInfo suggest(String columnName) {
        String normalized = normalize(columnName);
        if (IGNORED_COLUMNS.contains(normalized)) {
            return result(columnName, ItemImportTargetField.IGNORE, 1.0);
        }
        ItemImportTargetField bestField = ItemImportTargetField.IGNORE;
        double bestConfidence = 0.0;
        for (Map.Entry<ItemImportTargetField, List<String>> entry : PATTERNS.entrySet()) {
            for (String pattern : entry.getValue()) {
                double confidence = confidence(normalized, pattern);
                if (confidence > bestConfidence) {
                    bestField = entry.getKey();
                    bestConfidence = confidence;
                }
            }
        }
        return result(columnName, bestField, bestConfidence);
    }

    private double confidence(String column, String pattern) {
        if (column.equals(pattern)) return 1.0;
        if (column.contains(pattern) || pattern.contains(column)) {
            return 0.5 + 0.5 * pattern.length() / Math.max(column.length(), pattern.length());
        }
        return 0.0;
    }

    private String normalize(String value) {
        return value.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    private ItemImportColumnInfo result(String column, ItemImportTargetField field, double confidence) {
        return ItemImportColumnInfo.builder()
                .sourceColumn(column)
                .suggestedTargetField(field)
                .confidence(confidence)
                .build();
    }

    private static Map<ItemImportTargetField, List<String>> patterns() {
        Map<ItemImportTargetField, List<String>> result = new LinkedHashMap<>();
        result.put(ItemImportTargetField.NAME, List.of(
                "название", "наименование", "название товара", "наименование товара",
                "продукт", "услуга", "name", "product", "service", "title"));
        result.put(ItemImportTargetField.CATEGORY_LABEL, List.of(
                "категория", "категория товара", "категория услуги", "группа",
                "тип", "раздел", "category", "group", "type"));
        result.put(ItemImportTargetField.DESCRIPTION, List.of(
                "описание", "информация", "description", "info"));
        result.put(ItemImportTargetField.SKU, List.of(
                "артикул", "код товара", "код", "штрихкод", "sku", "article", "barcode"));
        result.put(ItemImportTargetField.PRICE, List.of(
                "цена", "стоимость", "прайс", "price", "cost"));
        result.put(ItemImportTargetField.TAGS, List.of(
                "теги", "тэги", "метки", "ключевые слова", "tags", "labels", "keywords"));
        return result;
    }
}
