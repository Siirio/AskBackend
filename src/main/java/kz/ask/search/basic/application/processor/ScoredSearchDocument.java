package kz.ask.search.basic.application.processor;

import java.util.List;
import java.util.Map;
import kz.ask.search.basic.domain.dto.SearchCandidateDto;
import kz.ask.search.basic.domain.dto.SearchDocumentDto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ScoredSearchDocument {

    private SearchDocumentDto document;
    private SearchCandidateDto candidate;
    private Integer score;
    private Map<String, Double> rankingFeatures;
    private String hypothesisId;
    private String sectionType;
    private String confidenceCode;
    private List<String> warnings;
    private Integer distanceMeters;
    private String distanceText;
    private String activeOfferLabel;
}
