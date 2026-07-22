package kz.ask.search.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kz.ask.search.domain.dto.SearchAiEnrichmentItem;
import kz.ask.search.domain.entity.SearchAiMetadata;
import kz.ask.search.domain.entity.SearchDocument;
import kz.ask.search.domain.enums.SearchAiMetadataSource;
import kz.ask.search.domain.enums.SearchAiVerificationState;
import kz.ask.search.domain.enums.SearchEventType;
import kz.ask.search.infrastructure.client.DeepSeekAttributeExtractor;
import kz.ask.search.infrastructure.repository.SearchAiMetadataRepository;
import kz.ask.search.infrastructure.repository.SearchDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchAiEnrichmentServiceImpl implements SearchAiEnrichmentService {

    private final SearchDocumentRepository documentRepository;
    private final SearchAiMetadataRepository metadataRepository;
    private final SearchOutboxService outboxService;
    private final DeepSeekAttributeExtractor extractor;
    private final ObjectMapper objectMapper;

    @Value("${ask.search.ai-enrichment.schema-version:attribute-v1}")
    private String schemaVersion;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(SearchAiEnrichmentItem item,
                         DeepSeekAttributeExtractor.ExtractionResult result) {
        SearchDocument document = documentRepository.findById(item.getDocumentId()).orElse(null);
        if (document == null
                || !item.getWorkerId().equals(document.getAiEnrichmentWorkerId())
                || !item.getUpdatedAtMillis().equals(document.getUpdatedAt().toEpochMilli())) {
            return;
        }

        List<SearchAiMetadata> existing = metadataRepository
                .findByAggregateTypeAndAggregateIdAndSource(
                        item.getAggregateType(), item.getAggregateId(), SearchAiMetadataSource.AI);
        List<SearchAiMetadata> replaceable = existing.stream()
                .filter(metadata -> schemaVersion.equals(metadata.getSchemaVersion()))
                .filter(metadata -> extractor.modelVersion().equals(metadata.getModelVersion()))
                .toList();
        metadataRepository.deleteAll(replaceable);

        Map<String, Object> aiAttributes = new HashMap<>();
        if (result != null) {
            Instant extractedAt = Instant.now();
            result.getFacts().forEach(fact -> {
                aiAttributes.put(fact.getKey(), objectMapper.convertValue(fact.getValue(), Object.class));
                metadataRepository.save(toMetadata(item, fact, extractedAt));
            });
            document.setAiSearchSummary(blankToNull(result.getSearchSummary()));
        } else {
            document.setAiSearchSummary(null);
        }
        document.setAiAttributes(aiAttributes);
        document.setAiEnrichmentVersion(item.getUpdatedAtMillis());
        document.setAiEnrichmentStartedAt(null);
        document.setAiEnrichmentWorkerId(null);
        document.setAiEnrichmentAttemptCount(0);
        document.setAiEnrichmentError(null);
        document.setAiEnrichmentDead(Boolean.FALSE);
        document.setAiEnrichmentRequested(Boolean.FALSE);
        outboxService.republish(
                item.getAggregateType(), item.getAggregateId(),
                SearchEventType.UPSERT, item.getUpdatedAtMillis());
    }

    private SearchAiMetadata toMetadata(SearchAiEnrichmentItem item,
                                        DeepSeekAttributeExtractor.ExtractionFact fact,
                                        Instant extractedAt) {
        SearchAiMetadata metadata = new SearchAiMetadata();
        metadata.setAggregateType(item.getAggregateType());
        metadata.setAggregateId(item.getAggregateId());
        metadata.setAttributeKey(fact.getKey());
        metadata.setAttributeValue(fact.getValue());
        metadata.setConfidence(fact.getConfidence());
        metadata.setEvidence(fact.getEvidence());
        metadata.setSource(SearchAiMetadataSource.AI);
        metadata.setModelVersion(extractor.modelVersion());
        metadata.setSchemaVersion(schemaVersion);
        metadata.setExtractedAt(extractedAt);
        metadata.setVerificationState(SearchAiVerificationState.AI_DERIVED);
        return metadata;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
