package kz.ask.search.infrastructure.scheduler;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import kz.ask.catalog.infrastructure.repository.ProductOfferRepository;
import kz.ask.catalog.infrastructure.repository.ProductRepository;
import kz.ask.search.domain.SearchIndexQueueServiceImpl;
import kz.ask.search.domain.entity.SearchIndexQueue;
import kz.ask.search.infrastructure.client.DeepSeekAttributeExtractor;
import kz.ask.search.infrastructure.repository.SearchDocumentRepository;
import kz.ask.search.infrastructure.repository.SearchIndexQueueRepository;
import kz.ask.service.infrastructure.repository.ServiceBranchOfferRepository;
import kz.ask.service.infrastructure.repository.ServiceOfferingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttributeExtractionScheduler {

    private static final int BATCH_SIZE = 50;
    private static final String ENTITY_TYPE_PRODUCT = "PRODUCT";
    private static final String ENTITY_TYPE_SERVICE = "SERVICE";

    private final SearchIndexQueueRepository queueRepository;
    private final SearchIndexQueueServiceImpl queueService;
    private final DeepSeekAttributeExtractor extractor;
    private final ProductRepository productRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;
    private final ProductOfferRepository productOfferRepository;
    private final ServiceBranchOfferRepository serviceBranchOfferRepository;
    private final SearchDocumentRepository searchDocumentRepository;

    @Scheduled(fixedDelayString = "${ask.search.indexer.interval-ms:60000}")
    public void processQueue() {
        List<SearchIndexQueue> entries = queueRepository.findScheduledBefore(
                java.time.Instant.now(), PageRequest.of(0, BATCH_SIZE));
        if (entries.isEmpty()) {
            return;
        }

        List<DeepSeekAttributeExtractor.ExtractionItem> items = entries.stream()
                .map(this::toExtractionItem)
                .filter(item -> item != null)
                .toList();

        if (items.isEmpty()) {
            entries.forEach(e -> queueService.markCompleted(e.getEntityType(), e.getEntityId()));
            return;
        }

        try {
            List<DeepSeekAttributeExtractor.ExtractionResult> results = extractor.extractBatch(items);
            Map<UUID, Map<String, Object>> resultMap = new java.util.HashMap<>();
            results.forEach(r -> resultMap.put(r.id(), r.attributes()));

            for (SearchIndexQueue entry : entries) {
                Map<String, Object> attrs = resultMap.get(entry.getEntityId());
                if (attrs != null) {
                    writeAttributes(entry.getEntityType(), entry.getEntityId(), attrs);
                    queueService.markCompleted(entry.getEntityType(), entry.getEntityId());
                } else {
                    queueService.markFailed(entry.getEntityType(), entry.getEntityId(),
                            "AI returned no attributes for this entity");
                }
            }
        } catch (Exception ex) {
            log.error("Batch attribute extraction failed: {}", ex.getMessage());
            entries.forEach(e -> queueService.markFailed(e.getEntityType(), e.getEntityId(), ex.getMessage()));
        }
    }

    private DeepSeekAttributeExtractor.ExtractionItem toExtractionItem(SearchIndexQueue entry) {
        if (ENTITY_TYPE_PRODUCT.equals(entry.getEntityType())) {
            return productRepository.findById(entry.getEntityId())
                    .map(p -> new DeepSeekAttributeExtractor.ExtractionItem(
                            p.getId(), p.getName(), p.getDescription(),
                            p.getCategoryLabel(), p.getTags()))
                    .orElse(null);
        }
        if (ENTITY_TYPE_SERVICE.equals(entry.getEntityType())) {
            return serviceOfferingRepository.findById(entry.getEntityId())
                    .map(s -> new DeepSeekAttributeExtractor.ExtractionItem(
                            s.getId(), s.getName(), s.getDescription(),
                            null, null))
                    .orElse(null);
        }
        return null;
    }

    private void writeAttributes(String entityType, UUID entityId, Map<String, Object> attrs) {
        if (ENTITY_TYPE_PRODUCT.equals(entityType)) {
            productRepository.findById(entityId).ifPresent(p -> {
                p.setAttributes(attrs);
                productRepository.save(p);
            });
            productOfferRepository.findByProductId(entityId).forEach(offer ->
                    searchDocumentRepository.findByProductOfferId(offer.getId()).ifPresent(doc -> {
                        doc.setAttributes(new java.util.HashMap<>(attrs));
                        searchDocumentRepository.save(doc);
                    }));
        } else if (ENTITY_TYPE_SERVICE.equals(entityType)) {
            serviceOfferingRepository.findById(entityId).ifPresent(s -> {
                s.setAttributes(attrs);
                serviceOfferingRepository.save(s);
            });
            serviceBranchOfferRepository.findByServiceOfferingId(entityId).forEach(sbo ->
                    searchDocumentRepository.findByServiceBranchOfferId(sbo.getId()).ifPresent(doc -> {
                        doc.setAttributes(new java.util.HashMap<>(attrs));
                        searchDocumentRepository.save(doc);
                    }));
        }
    }
}
