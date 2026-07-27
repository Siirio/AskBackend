package kz.ask.search.basic.application.processor;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchInterpretation {

    private String normalizedQuery;
    private BigDecimal inferredMinPrice;
    private BigDecimal inferredMaxPrice;
    private String inferredCity;
    private String ambiguity;
    private List<String> suggestions;
}
