package kz.ask.search.ai_driven.search_query_enrichment.domain;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class IntentCategoryMapper {

    private static final Map<String, List<String>> CATEGORY_ALIASES = Map.ofEntries(
            Map.entry("beauty_services", List.of("beauty services", "бьюти услуги", "бьюти-услуги", "услуги красоты", "салон красоты", "косметология")),
            Map.entry("haircut", List.of("haircut", "стрижка", "мужская стрижка", "женская стрижка", "подстричься", "парикмахерская")),
            Map.entry("barbershop", List.of("barbershop", "барбершоп", "барбер", "борода", "grooming")),
            Map.entry("hair_salon", List.of("hair salon", "салон красоты", "парикмахерская", "укладка", "окрашивание")),
            Map.entry("gaming_club", List.of("gaming club", "ps клуб", "ps5", "playstation", "игровой клуб", "поиграть в ps")),
            Map.entry("computer_club", List.of("computer club", "компьютерный клуб", "киберклуб", "игровой компьютер")),
            Map.entry("sports_nutrition", List.of("sports nutrition", "спортпит", "спортивное питание", "добавки")),
            Map.entry("creatine", List.of("creatine", "креатин", "моногидрат", "добавка для тренировок")),
            Map.entry("cosmetics", List.of("cosmetics", "косметика", "корейская косметика", "уход за кожей", "крем")),
            Map.entry("laptop", List.of("laptop", "ноутбук", "ноут", "ультрабук")),
            Map.entry("electronics", List.of("electronics", "электроника", "техника", "гаджеты")),
            Map.entry("watches", List.of("watch", "watches", "часы", "смарт часы", "smart watch", "apple watch"))
    );

    public List<StructuredCategorySignal> map(List<String> terms) {
        Set<String> normalizedTerms = new LinkedHashSet<>();
        terms.stream().map(this::normalize).filter(term -> !term.isBlank()).forEach(normalizedTerms::add);
        List<StructuredCategorySignal> signals = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : CATEGORY_ALIASES.entrySet()) {
            if (matches(normalizedTerms, entry.getValue())) {
                signals.add(StructuredCategorySignal.builder()
                        .canonicalKey(entry.getKey())
                        .aliases(entry.getValue())
                        .confidence(0.85)
                        .build());
            }
        }
        return signals;
    }

    private Boolean matches(Set<String> terms, List<String> aliases) {
        for (String term : terms) {
            for (String alias : aliases) {
                String normalizedAlias = normalize(alias);
                if (term.contains(normalizedAlias) || normalizedAlias.contains(term)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
