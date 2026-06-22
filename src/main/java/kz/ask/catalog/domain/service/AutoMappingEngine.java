package kz.ask.catalog.domain.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kz.ask.catalog.domain.enums.TargetField;
import org.springframework.stereotype.Component;

@Component
public class AutoMappingEngine {

    private static final Map<TargetField, List<String>> PATTERNS = new LinkedHashMap<>();
    private static final Set<String> FORCED_IGNORE = Set.of(
        "остаток", "наличие", "количество", "склад",
        "stock", "quantity", "warehouse", "available", "availability"
    );

    static {
        PATTERNS.put(TargetField.NAME, List.of(
            "название", "наименование", "название товара", "наименование товара",
            "продукт", "имя товара", "наименование продукта",
            "name", "product", "title", "product name"
        ));
        PATTERNS.put(TargetField.CATEGORY_LABEL, List.of(
            "категория", "категория товара", "группа", "группа товара",
            "тип товара", "раздел", "вид товара",
            "category", "group", "type"
        ));
        PATTERNS.put(TargetField.DESCRIPTION, List.of(
            "описание", "описание товара", "краткое описание",
            "description", "info"
        ));
        PATTERNS.put(TargetField.SKU, List.of(
            "артикул", "код товара", "код", "шк", "штрихкод",
            "sku", "article", "barcode", "vendor code", "арт",
            "артикул/код", "артикул код"
        ));
        PATTERNS.put(TargetField.PRICE, List.of(
            "цена", "цена продажи", "розничная цена", "розница",
            "стоимость", "прайс",
            "price", "cost", "retail price"
        ));
        PATTERNS.put(TargetField.TAGS, List.of(
            "теги", "тэги", "метки", "ключевые слова",
            "tags", "labels", "keywords", "тэг"
        ));
    }

    public MappingSuggestion suggest(String columnName) {
        String col = normalize(columnName);

        if (FORCED_IGNORE.contains(col)) {
            return new MappingSuggestion(TargetField.IGNORE, 1.0);
        }

        MappingSuggestion best = null;

        for (var entry : PATTERNS.entrySet()) {
            for (String pattern : entry.getValue()) {
                if (col.equals(pattern)) {
                    double conf = 1.0;
                    if (best == null || conf > best.confidence()) {
                        best = new MappingSuggestion(entry.getKey(), conf);
                    }
                } else if (col.contains(pattern) || pattern.contains(col)) {
                    double conf = 0.5 + (0.5 * pattern.length() / Math.max(col.length(), pattern.length()));
                    if (best == null || conf > best.confidence()) {
                        best = new MappingSuggestion(entry.getKey(), conf);
                    }
                }
            }
        }

        return best != null ? best : new MappingSuggestion(TargetField.IGNORE, 0.0);
    }

    private String normalize(String s) {
        return s.trim().toLowerCase().replaceAll("\\s+", " ");
    }
}
