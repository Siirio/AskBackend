package kz.ask.search.basic.infrastructure.meilisearch;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import kz.ask.search.basic.domain.dto.SearchCandidateDto;
import kz.ask.search.basic.domain.dto.SearchCandidateSetDto;
import org.springframework.stereotype.Component;

@Component
public class ReciprocalRankFusionPolicy {

    private static final Integer FUSION_CONSTANT = 60;

    public SearchCandidateSetDto fuse(
            List<UUID> rawLexicalLane,
            List<UUID> expandedLexicalLane,
            List<UUID> semanticLane,
            int limit) {
        Map<UUID, Double> scores = new LinkedHashMap<>();
        addLaneScores(scores, rawLexicalLane);
        addLaneScores(scores, expandedLexicalLane);
        addLaneScores(scores, semanticLane);
        List<SearchCandidateDto> candidates = scores.entrySet().stream()
                .sorted(Map.Entry.<UUID, Double>comparingByValue().reversed()
                        .thenComparing(entry -> entry.getKey().toString()))
                .limit(limit)
                .map(entry -> SearchCandidateDto.builder()
                        .aggregateId(entry.getKey())
                        .rawLexicalRank(rankOf(rawLexicalLane, entry.getKey()))
                        .expandedLexicalRank(rankOf(expandedLexicalLane, entry.getKey()))
                        .semanticRank(rankOf(semanticLane, entry.getKey()))
                        .rawLexicalScore(scoreOf(rawLexicalLane, entry.getKey()))
                        .expandedLexicalScore(scoreOf(expandedLexicalLane, entry.getKey()))
                        .semanticScore(scoreOf(semanticLane, entry.getKey()))
                        .fusionScore(entry.getValue())
                        .build())
                .toList();
        return SearchCandidateSetDto.builder()
                .candidates(candidates)
                .semanticLaneAvailable(!semanticLane.isEmpty())
                .build();
    }

    private void addLaneScores(Map<UUID, Double> scores, List<UUID> lane) {
        for (int index = 0; index < lane.size(); index++) {
            scores.merge(lane.get(index), 1.0 / (FUSION_CONSTANT + index + 1), Double::sum);
        }
    }

    private Integer rankOf(List<UUID> lane, UUID aggregateId) {
        int index = lane.indexOf(aggregateId);
        return index < 0 ? null : index + 1;
    }

    private Double scoreOf(List<UUID> lane, UUID aggregateId) {
        Integer rank = rankOf(lane, aggregateId);
        return rank == null ? null : 1.0 / (FUSION_CONSTANT + rank);
    }
}
