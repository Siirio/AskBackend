package kz.ask.search.application.processor;

import java.util.List;
import kz.ask.search.domain.entity.SearchDocument;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ScoredSearchDocument {

    private SearchDocument document;
    private Integer score;
    private String sectionType;
    private String confidenceCode;
    private List<String> matchReasons;
    private List<String> warnings;
}
