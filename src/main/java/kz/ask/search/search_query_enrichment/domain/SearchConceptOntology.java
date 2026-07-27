package kz.ask.search.search_query_enrichment.domain;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import kz.ask.search.basic.domain.enums.SearchConcept;
import org.springframework.stereotype.Component;

@Component
public class SearchConceptOntology {

    private static final String VERSION = "controlled-ontology-v1";
    private static final Map<SearchConcept, List<String>> TRIGGERS = Map.ofEntries(
            Map.entry(SearchConcept.RIDE_ACTIVITY, List.of(
                    "покат", "погонять", "картинг", "картодром", "велосипед",
                    "лошад", "конн", "квадроцикл", "ролик", "коньк")),
            Map.entry(SearchConcept.MOTORSPORT, List.of(
                    "картинг", "картодром", "гонк", "погонять", "квадроцикл")),
            Map.entry(SearchConcept.ACTIVE_LEISURE, List.of(
                    "активн", "покат", "спорт", "гонк", "прогулк")),
            Map.entry(SearchConcept.GROUP_ACTIVITY, List.of(
                    "друз", "компани", "вместе", "достар")),
            Map.entry(SearchConcept.KIDS_ACTIVITY, List.of(
                    "дет", "ребен", "балалар", "семь")),
            Map.entry(SearchConcept.DATE_ACTIVITY, List.of(
                    "свидан", "вдвоем", "пара")),
            Map.entry(SearchConcept.INDOOR_ENTERTAINMENT, List.of(
                    "крыт", "помещен", "клуб", "игр")),
            Map.entry(SearchConcept.GAMING, List.of(
                    "игр", "gaming", "ps5", "компьютерн")),
            Map.entry(SearchConcept.BEAUTY, List.of(
                    "красот", "салон", "макияж", "стриж", "барбер")),
            Map.entry(SearchConcept.CLEANING, List.of(
                    "уборк", "клининг", "чист")),
            Map.entry(SearchConcept.REPAIR, List.of(
                    "ремонт", "почин", "сервис")),
            Map.entry(SearchConcept.EDUCATION, List.of(
                    "обуч", "курс", "урок", "репетитор")),
            Map.entry(SearchConcept.HEALTHCARE, List.of(
                    "врач", "лечен", "клиник", "здоров")));

    private static final Map<SearchConcept, List<String>> EXPANSIONS = Map.ofEntries(
            Map.entry(SearchConcept.RIDE_ACTIVITY, List.of(
                    "покататься", "картинг", "прокат велосипедов", "конные прогулки",
                    "ролики", "каток", "квадроциклы")),
            Map.entry(SearchConcept.MOTORSPORT, List.of(
                    "картинг", "картодром", "гонки на картах", "квадроциклы")),
            Map.entry(SearchConcept.ACTIVE_LEISURE, List.of(
                    "активный отдых", "активный досуг", "развлечения")),
            Map.entry(SearchConcept.GROUP_ACTIVITY, List.of(
                    "куда сходить с друзьями", "развлечения для компании")),
            Map.entry(SearchConcept.KIDS_ACTIVITY, List.of(
                    "детские развлечения", "куда сходить с детьми")),
            Map.entry(SearchConcept.DATE_ACTIVITY, List.of(
                    "куда сходить на свидание", "развлечения для двоих")),
            Map.entry(SearchConcept.INDOOR_ENTERTAINMENT, List.of(
                    "развлечения в помещении", "крытая площадка")));

    private static final Map<SearchConcept, List<String>> USE_CASES = Map.of(
            SearchConcept.GROUP_ACTIVITY, List.of("куда сходить с друзьями"),
            SearchConcept.KIDS_ACTIVITY, List.of("куда сходить с детьми"),
            SearchConcept.DATE_ACTIVITY, List.of("куда сходить на свидание"),
            SearchConcept.ACTIVE_LEISURE, List.of("активно провести время", "активно провести вечер"),
            SearchConcept.RIDE_ACTIVITY, List.of("покататься", "развлечение на выходных"));

    public String version() {
        return VERSION;
    }

    public List<String> resolveConceptIds(String text) {
        return resolveConcepts(text).stream().map(Enum::name).toList();
    }

    public List<String> resolveExpansions(String text) {
        Set<String> values = new LinkedHashSet<>();
        resolveConcepts(text).forEach(concept -> values.addAll(
                EXPANSIONS.getOrDefault(concept, List.of())));
        return List.copyOf(values);
    }

    public List<String> resolveUseCases(String text) {
        Set<String> values = new LinkedHashSet<>();
        resolveConcepts(text).forEach(concept -> values.addAll(
                USE_CASES.getOrDefault(concept, List.of())));
        return List.copyOf(values);
    }

    public String normalizeQuery(String rawQuery) {
        String normalized = normalize(rawQuery);
        if (normalized.contains("покат")) {
            return "покататься";
        }
        return normalized;
    }

    private List<SearchConcept> resolveConcepts(String text) {
        String normalized = normalize(text);
        List<SearchConcept> concepts = new ArrayList<>();
        TRIGGERS.forEach((concept, triggers) -> {
            if (containsAny(normalized, triggers)) {
                concepts.add(concept);
            }
        });
        return concepts;
    }

    private boolean containsAny(String value, List<String> terms) {
        return terms.stream().anyMatch(value::contains);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT)
                .replace('ё', 'е')
                .replaceAll("\\s+", " ");
    }
}
