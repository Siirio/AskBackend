package kz.ask.search.basic.domain.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchHitDto {

    private UUID aggregateId;
    private Double rankingScore;
}
