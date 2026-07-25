package kz.ask.search.basic.application.processor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import kz.ask.search.basic.domain.enums.SearchDocumentType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class SearchPlan {

    private SearchDocumentType itemType;
    private String city;
    private String possibleCity;
    private String userSelectedCategory;
    private String sort;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
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
    private List<String> mustHave;
    private List<String> niceToHave;
    private List<String> notWanted;
    private List<String> rankingPriorities;
    private Map<String, Object> intentAttributes;
}
