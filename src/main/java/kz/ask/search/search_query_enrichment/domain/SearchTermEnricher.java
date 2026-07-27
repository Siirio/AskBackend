package kz.ask.search.search_query_enrichment.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchTermEnricher {

    private static final Integer MAX_INDEX_TOKEN_LENGTH = 255;

    private final SearchConceptOntology searchConceptOntology;

    public List<String> enrichIndexTerms(Collection<String> sourceTerms) {
        Set<String> terms = new LinkedHashSet<>();
        sourceTerms.forEach(sourceTerm -> addTokens(terms, sourceTerm));
        searchConceptOntology.resolveExpansions(String.join(" ", sourceTerms))
                .forEach(term -> addToken(terms, term));
        return new ArrayList<>(terms);
    }

    public List<String> expandIntentTerms(String sourceText) {
        return searchConceptOntology.resolveExpansions(sourceText);
    }

    public Boolean isKnownCommercialType(String sourceText) {
        return !searchConceptOntology.resolveConceptIds(sourceText).isEmpty();
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

    private void addToken(Set<String> terms, String token) {
        String normalized = normalize(token);
        if (!normalized.isBlank() && normalized.length() <= MAX_INDEX_TOKEN_LENGTH) {
            terms.add(normalized);
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
