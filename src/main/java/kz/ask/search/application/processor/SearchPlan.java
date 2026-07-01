package kz.ask.search.application.processor;

import java.math.BigDecimal;
import java.util.List;
import kz.ask.search.domain.enums.SearchDocumentType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchPlan {

    private SearchDocumentType itemType;
    private String city;
    private String userSelectedCategory;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal minPackageGrams;
    private BigDecimal maxPackageGrams;
    private List<String> canonicalCategoryKeys;
    private List<String> categoryAliases;
    private List<String> hardMatchTerms;
    private List<String> qualifierTerms;
    private List<String> exactTerms;
    private List<String> semanticTerms;
    private List<String> synonyms;
    private List<String> relatedTerms;
    private List<String> mustHave;
    private List<String> niceToHave;
    private List<String> notWanted;
    private List<String> rankingPriorities;
}
