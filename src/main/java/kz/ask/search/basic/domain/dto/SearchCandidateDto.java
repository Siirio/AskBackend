package kz.ask.search.basic.domain.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchCandidateDto {

    private UUID aggregateId;
    private Integer rawLexicalRank;
    private Integer expandedLexicalRank;
    private Integer semanticRank;
    private Double rawLexicalScore;
    private Double expandedLexicalScore;
    private Double semanticScore;
    private Double fusionScore;
}
