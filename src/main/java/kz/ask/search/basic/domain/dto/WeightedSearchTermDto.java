package kz.ask.search.basic.domain.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WeightedSearchTermDto {

    private String value;
    private BigDecimal weight;
    private String source;
}
