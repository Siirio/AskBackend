package kz.ask.search.basic.domain.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchIntentHypothesisDto {

    private String intentId;
    private BigDecimal probability;
    private List<WeightedSearchTermDto> terms;
    private List<WeightedConceptDto> concepts;
}
