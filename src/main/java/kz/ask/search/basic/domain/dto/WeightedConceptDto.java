package kz.ask.search.basic.domain.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WeightedConceptDto {

    private String conceptId;
    private BigDecimal weight;
}
