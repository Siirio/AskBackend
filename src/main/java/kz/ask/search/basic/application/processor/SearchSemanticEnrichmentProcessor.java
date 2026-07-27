package kz.ask.search.basic.application.processor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import kz.ask.ai.infrastructure.client.DeepSeekAttributeExtractor;
import kz.ask.search.basic.application.SearchProjectionComposer;
import kz.ask.search.basic.domain.dto.SearchDocumentDto;
import kz.ask.search.search_query_enrichment.domain.SearchConceptOntology;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchSemanticEnrichmentProcessor {

    private static final BigDecimal ONTOLOGY_CONFIDENCE = new BigDecimal("0.700");

    private final DeepSeekAttributeExtractor extractor;
    private final SearchProjectionComposer searchProjectionComposer;
    private final SearchSemanticMetadataMutationProcessor mutationProcessor;
    private final SearchConceptOntology searchConceptOntology;

    public Boolean enrichIfRequired(SearchDocumentDto projection) {
        String targetModelVersion = extractor.isAvailable()
                ? extractor.modelVersion() : searchConceptOntology.version();
        if (projection.getSemanticSourceHash().equals(projection.getSemanticMetadataSourceHash())
                && targetModelVersion.equals(projection.getSemanticModelVersion())) {
            return false;
        }
        SearchDocumentDto enriched = extractor.isAvailable()
                ? enrichWithAi(projection)
                : enrichWithOntology(projection);
        mutationProcessor.replace(enriched, projection.getProjectionVersion());
        return true;
    }

    private SearchDocumentDto enrichWithAi(SearchDocumentDto projection) {
        try {
            List<DeepSeekAttributeExtractor.ExtractionResult> results = extractor.extractBatch(List.of(
                    DeepSeekAttributeExtractor.ExtractionItem.builder()
                            .id(projection.getAggregateId())
                            .title(projection.getTitle())
                            .description(projection.getSummary())
                            .categoryLabel(projection.getCategoryLabel())
                            .aliases(projection.getAliases())
                            .build()));
            if (results.isEmpty()) {
                return enrichWithOntology(projection);
            }
            DeepSeekAttributeExtractor.ExtractionResult result = results.get(0);
            List<String> evidence = new ArrayList<>(result.getEvidence());
            result.getFacts().stream()
                    .map(DeepSeekAttributeExtractor.ExtractionFact::getEvidence)
                    .filter(StringUtils::hasText)
                    .forEach(evidence::add);
            return searchProjectionComposer.applySemanticMetadata(
                    projection,
                    result.getAliases(),
                    result.getConceptIds(),
                    result.getUseCases(),
                    result.getSearchSummary(),
                    result.getConfidence(),
                    evidence,
                    extractor.modelVersion(),
                    Instant.now());
        } catch (RuntimeException failure) {
            log.warn("Search semantic metadata enrichment is unavailable for {}: {}",
                    projection.getAggregateId(), failure.getMessage());
            return enrichWithOntology(projection);
        }
    }

    private SearchDocumentDto enrichWithOntology(SearchDocumentDto projection) {
        String source = String.join(" ",
                projection.getTitle(),
                projection.getSummary(),
                projection.getCategoryLabel());
        return searchProjectionComposer.applySemanticMetadata(
                projection,
                searchConceptOntology.resolveExpansions(source),
                searchConceptOntology.resolveConceptIds(source),
                searchConceptOntology.resolveUseCases(source),
                projection.getSummary(),
                ONTOLOGY_CONFIDENCE,
                List.of(projection.getTitle(), projection.getCategoryLabel()),
                searchConceptOntology.version(),
                Instant.now());
    }
}
