package kz.ask.search.decision.api.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CompareItemResponse {

    private UUID resultId;
    private String title;
    private String image;
    private BigDecimal price;
    private String currency;
    private String verdict;
}
