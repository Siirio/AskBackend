package kz.ask.search.basic.application.processor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import kz.ask.search.basic.domain.dto.SearchIntentHypothesisDto;
import kz.ask.search.basic.domain.dto.WeightedConceptDto;
import kz.ask.search.basic.domain.dto.WeightedSearchTermDto;
import kz.ask.search.basic.domain.enums.SearchDocumentType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class SearchPlan {

    private String rawQuery;
    private String semanticQuery;
    private SearchDocumentType itemType;
    private String city;
    private String country;
    private String possibleCity;
    private String userSelectedCategory;
    private String sort;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean openNow;
    private Integer radiusMeters;
    private Double userLatitude;
    private Double userLongitude;
    private BigDecimal possibleMinPrice;
    private BigDecimal possibleMaxPrice;
    private BigDecimal minPackageGrams;
    private BigDecimal maxPackageGrams;
    private List<String> canonicalCategoryKeys;
    private List<String> categoryAliases;
    private List<String> hardMatchTerms;
    private List<String> qualifierTerms;
    private List<String> exactTerms;
    private List<String> expandedTerms;
    private List<String> aiSynonyms;
    private List<String> relatedTerms;
    private List<String> conceptIds;
    private List<WeightedSearchTermDto> weightedTerms;
    private List<WeightedConceptDto> weightedConcepts;
    private String ambiguity;
    private List<String> clarificationSuggestions;
    private List<SearchIntentHypothesisDto> hypotheses;
    private List<String> mustHave;
    private List<String> niceToHave;
    private List<String> notWanted;
    private List<String> rankingPriorities;
    private Map<String, Object> intentAttributes;
}
