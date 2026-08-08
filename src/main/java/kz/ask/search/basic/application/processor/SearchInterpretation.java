package kz.ask.search.basic.application.processor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Getter
@Builder
public class SearchInterpretation {

    private String normalizedQuery;
    private BigDecimal inferredMinPrice;
    private BigDecimal inferredMaxPrice;
    private String inferredCity;
    private String ambiguity;
    private List<String> suggestions;

    @Singular("mustHaveItem")
    private List<InterpretedCriterion> mustHave;

    @Singular("niceToHaveItem")
    private List<InterpretedCriterion> niceToHave;

    @Singular("notWantedItem")
    private List<InterpretedCriterion> notWanted;

    @Singular
    private List<InterpretedUseCase> useCases;

    private Map<String, Object> normalizedAttributes;

    @Singular
    private List<String> searchKeywords;

    @Singular
    private List<String> relatedTerms;

    @Singular
    private List<String> rankingHints;
}
