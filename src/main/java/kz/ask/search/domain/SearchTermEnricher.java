package kz.ask.search.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class SearchTermEnricher {

    private static final Integer MAX_INDEX_TOKEN_LENGTH = 255;
    private static final List<String> SPORT_NUTRITION_BROAD = List.of(
            "спортпит", "спортивное питание", "sports nutrition", "добавки", "бад"
    );
    private static final List<String> CREATINE = List.of("креатин", "creatine", "моногидрат");
    private static final List<String> PROTEIN = List.of("протеин", "protein", "сывороточный протеин", "whey");
    private static final List<String> GAINER = List.of("гейнер", "gainer");
    private static final List<String> PRE_WORKOUT = List.of(
            "предтреник", "предтренировочный комплекс", "preworkout", "pre workout"
    );
    private static final List<String> PROTEIN_BAR = List.of("батончик", "батончики", "protein bar");
    private static final List<String> AMINO_ACIDS = List.of("аминокислоты", "аминокислота", "bcaa", "eaa");
    private static final List<String> VITAMINS = List.of("витамины", "витамин", "vitamins");
    private static final List<String> BIKE_RENTAL = List.of(
            "велики", "велик", "велосипед", "велосипеды", "прокат велосипедов", "аренда велосипедов",
            "прокат великов", "bike rental", "bicycle rental"
    );

    public List<String> enrichIndexTerms(Collection<String> sourceTerms) {
        Set<String> terms = new LinkedHashSet<>();
        for (String sourceTerm : sourceTerms) {
            addTokens(terms, sourceTerm);
        }
        addSportNutritionIndexTerms(terms, normalize(String.join(" ", terms)));
        addBikeRentalIndexTerms(terms, normalize(String.join(" ", terms)));
        return new ArrayList<>(terms);
    }

    public List<String> expandIntentTerms(String sourceText) {
        String normalized = normalize(sourceText);
        Set<String> terms = new LinkedHashSet<>();
        addSpecificSportNutritionIntentTerms(terms, normalized);
        addBikeRentalIntentTerms(terms, normalized);
        if (terms.isEmpty() && containsAny(normalized, SPORT_NUTRITION_BROAD)) {
            addAll(terms, SPORT_NUTRITION_BROAD);
            addAll(terms, CREATINE);
            addAll(terms, PROTEIN);
            addAll(terms, GAINER);
            addAll(terms, PRE_WORKOUT);
            addAll(terms, PROTEIN_BAR);
            addAll(terms, AMINO_ACIDS);
            addAll(terms, VITAMINS);
        }
        return new ArrayList<>(terms);
    }

    public Boolean isKnownCommercialType(String sourceText) {
        String normalized = normalize(sourceText);
        return containsAny(normalized, SPORT_NUTRITION_BROAD)
                || containsAny(normalized, CREATINE)
                || containsAny(normalized, PROTEIN)
                || containsAny(normalized, GAINER)
                || containsAny(normalized, PRE_WORKOUT)
                || containsAny(normalized, PROTEIN_BAR)
                || containsAny(normalized, AMINO_ACIDS)
                || containsAny(normalized, VITAMINS)
                || containsAny(normalized, BIKE_RENTAL);
    }

    private void addSportNutritionIndexTerms(Set<String> terms, String sourceText) {
        Set<String> specificTerms = new LinkedHashSet<>();
        addSpecificSportNutritionIntentTerms(specificTerms, sourceText);
        if (specificTerms.isEmpty() && !containsAny(sourceText, SPORT_NUTRITION_BROAD)) {
            return;
        }
        addAll(terms, SPORT_NUTRITION_BROAD);
        addAll(terms, specificTerms);
    }

    private void addSpecificSportNutritionIntentTerms(Set<String> terms, String sourceText) {
        if (containsAny(sourceText, CREATINE)) {
            addAll(terms, CREATINE);
        }
        if (containsAny(sourceText, PROTEIN)) {
            addAll(terms, PROTEIN);
        }
        if (containsAny(sourceText, GAINER)) {
            addAll(terms, GAINER);
        }
        if (containsAny(sourceText, PRE_WORKOUT)) {
            addAll(terms, PRE_WORKOUT);
        }
        if (containsAny(sourceText, PROTEIN_BAR)) {
            addAll(terms, PROTEIN_BAR);
        }
        if (containsAny(sourceText, AMINO_ACIDS)) {
            addAll(terms, AMINO_ACIDS);
        }
        if (containsAny(sourceText, VITAMINS)) {
            addAll(terms, VITAMINS);
        }
    }

    private void addBikeRentalIndexTerms(Set<String> terms, String sourceText) {
        if (containsAny(sourceText, BIKE_RENTAL)) {
            addAll(terms, BIKE_RENTAL);
        }
    }

    private void addBikeRentalIntentTerms(Set<String> terms, String sourceText) {
        if (containsAny(sourceText, BIKE_RENTAL)) {
            addAll(terms, BIKE_RENTAL);
        }
    }

    private void addTokens(Set<String> terms, String text) {
        String normalized = normalize(text);
        if (normalized.isBlank()) {
            return;
        }
        addToken(terms, normalized);
        for (String token : normalized.split("[\\s,;]+")) {
            addToken(terms, token);
        }
    }

    private void addAll(Set<String> terms, Collection<String> sourceTerms) {
        sourceTerms.stream().map(this::normalize).forEach(term -> addToken(terms, term));
    }

    private void addToken(Set<String> terms, String token) {
        String normalized = normalize(token);
        if (!normalized.isBlank() && normalized.length() <= MAX_INDEX_TOKEN_LENGTH) {
            terms.add(normalized);
        }
    }

    private Boolean containsAny(String value, Collection<String> terms) {
        return terms.stream().map(this::normalize).anyMatch(term -> !term.isBlank() && value.contains(term));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
