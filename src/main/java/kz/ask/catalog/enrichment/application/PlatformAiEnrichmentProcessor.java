package kz.ask.catalog.enrichment.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import kz.ask.business.uniqueoffer.domain.entity.UniqueOffer;
import kz.ask.business.uniqueoffer.infrastructure.repository.UniqueOfferRepository;
import kz.ask.business.core.domain.enums.BusinessScope;
import kz.ask.identity.authorization.domain.enums.Permission;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.managedimport.domain.ManagedImportService;
import kz.ask.offer.item.domain.entity.Item;
import kz.ask.offer.item.infrastructure.repository.ProductRepository;
import kz.ask.offer.service.infrastructure.repository.ServiceOfferingRepository;
import kz.ask.platform.domain.PlatformMembershipService;
import kz.ask.catalog.enrichment.api.dto.RequestAiEnrichmentRequest;
import kz.ask.catalog.enrichment.api.dto.RequestAiEnrichmentResponse;
import kz.ask.ai.infrastructure.client.DeepSeekAttributeExtractor;
import kz.ask.search.basic.domain.SearchOutboxService;
import kz.ask.search.basic.domain.SearchDocumentService;
import kz.ask.search.basic.application.SearchProjectionComposer;
import kz.ask.search.basic.domain.dto.SearchDocumentDto;
import kz.ask.search.basic.domain.enums.SearchAggregateType;
import kz.ask.search.basic.domain.enums.SearchDocumentType;
import kz.ask.search.basic.domain.enums.SearchEventType;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class PlatformAiEnrichmentProcessor {

    private final PlatformMembershipService platformMembershipService;
    private final ManagedImportService managedImportService;
    private final ProductRepository productRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;
    private final UniqueOfferRepository uniqueOfferRepository;
    private final SearchOutboxService searchOutboxService;
    private final SearchDocumentService searchDocumentService;
    private final SearchProjectionComposer searchProjectionComposer;
    private final DeepSeekAttributeExtractor extractor;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;

    public RequestAiEnrichmentResponse request(
            AskPrincipal principal,
            RequestAiEnrichmentRequest request) {
        var membership = platformMembershipService.findActiveByUser(principal.getUserId());
        if (membership == null
                || !membership.getPermissions().contains(Permission.USE_AI_ITEMS_SERVICES_TOOLS)) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
        return switch (request.getTargetType()) {
            case ITEM -> enrichProducts(principal.getUserId(), request.getAggregateIds());
            case SERVICE -> enrichServices(principal.getUserId(), request.getAggregateIds());
            case UNIQUE_OFFER -> enrichUniqueOffers(principal.getUserId(), request.getAggregateIds());
        };
    }

    private RequestAiEnrichmentResponse enrichProducts(UUID platformUserId, Iterable<UUID> ids) {
        List<UUID> targetIds = toList(ids);
        List<DeepSeekAttributeExtractor.ExtractionItem> inputs = transactionTemplate.execute(status -> {
            List<Item> items = productRepository.findByIdIn(targetIds);
            items.forEach(item -> requireActiveGrant(
                    platformUserId, item.getBusiness().getId(), BusinessScope.ITEM));
            return items.stream()
                    .map(item -> toExtractionItem(item.getId(), item.getName(), item.getDescription(),
                            item.getCategoryLabel(), item.getTags()))
                    .toList();
        });
        if (!extractor.isAvailable()) {
            return response(0);
        }
        Map<UUID, DeepSeekAttributeExtractor.ExtractionResult> results = extract(inputs);
        transactionTemplate.executeWithoutResult(status -> {
            List<Item> currentItems = productRepository.findByIdIn(targetIds);
            currentItems.forEach(item -> {
                requireActiveGrant(platformUserId, item.getBusiness().getId(), BusinessScope.ITEM);
                DeepSeekAttributeExtractor.ExtractionResult result = results.get(item.getId());
                if (result == null) {
                    return;
                }
                apply(item, result);
                syncItemProjection(item);
            });
        });
        return response(results.size());
    }

    private RequestAiEnrichmentResponse enrichServices(UUID platformUserId, Iterable<UUID> ids) {
        List<UUID> targetIds = toList(ids);
        List<DeepSeekAttributeExtractor.ExtractionItem> inputs = transactionTemplate.execute(status -> {
            List<kz.ask.offer.service.domain.entity.Service> services =
                    serviceOfferingRepository.findByIdIn(targetIds);
            services.forEach(service -> requireActiveGrant(
                    platformUserId, service.getBusiness().getId(), BusinessScope.SERVICE));
            return services.stream()
                    .map(service -> toExtractionItem(service.getId(), service.getName(), service.getDescription(),
                            service.getCategoryLabel(), List.of()))
                    .toList();
        });
        if (!extractor.isAvailable()) {
            return response(0);
        }
        Map<UUID, DeepSeekAttributeExtractor.ExtractionResult> results = extract(inputs);
        transactionTemplate.executeWithoutResult(status -> {
            List<kz.ask.offer.service.domain.entity.Service> currentServices =
                    serviceOfferingRepository.findByIdIn(targetIds);
            currentServices.forEach(service -> {
                requireActiveGrant(platformUserId, service.getBusiness().getId(), BusinessScope.SERVICE);
                DeepSeekAttributeExtractor.ExtractionResult result = results.get(service.getId());
                if (result == null) {
                    return;
                }
                apply(service, result);
                syncServiceProjection(service);
            });
        });
        return response(results.size());
    }

    private RequestAiEnrichmentResponse enrichUniqueOffers(UUID platformUserId, Iterable<UUID> ids) {
        List<UUID> targetIds = toList(ids);
        List<DeepSeekAttributeExtractor.ExtractionItem> inputs = transactionTemplate.execute(status -> {
            List<UniqueOffer> offers = uniqueOfferRepository.findByIdIn(targetIds);
            offers.forEach(offer -> requireActiveGrant(platformUserId, offer.getBusiness().getId(), null));
            return offers.stream()
                    .map(offer -> toExtractionItem(offer.getId(), offer.getName(), offer.getDescription(),
                            offer.getType().name(), offer.getTags()))
                    .toList();
        });
        if (!extractor.isAvailable()) {
            return response(0);
        }
        Map<UUID, DeepSeekAttributeExtractor.ExtractionResult> results = extract(inputs);
        transactionTemplate.executeWithoutResult(status -> {
            List<UniqueOffer> currentOffers = uniqueOfferRepository.findByIdIn(targetIds);
            currentOffers.forEach(offer -> {
                requireActiveGrant(platformUserId, offer.getBusiness().getId(), null);
                DeepSeekAttributeExtractor.ExtractionResult result = results.get(offer.getId());
                if (result != null) {
                    apply(offer, result);
                }
            });
        });
        return response(results.size());
    }

    private void syncItemProjection(Item item) {
        boolean searchable = Boolean.TRUE.equals(item.getIsActive())
                && item.getModerationStatus() == kz.ask.offer.item.domain.enums.ProductModerationStatus.APPROVED;
        if (!searchable) {
            Long version = searchDocumentService.delete(SearchDocumentType.ITEM, item.getId());
            searchOutboxService.publish(SearchAggregateType.ITEM, item.getId(), SearchEventType.DELETE, version);
            return;
        }
        SearchDocumentDto projection = searchProjectionComposer.composeItem(
                item.getId(), item.getBusiness().getId(),
                item.getBranch() == null ? null : item.getBranch().getId(),
                item.getName(), item.getDescription(), item.getCategoryLabel(),
                item.getBusiness().getName(), item.getBranch() == null ? null : item.getBranch().getName(),
                item.getPrice(), item.getBusiness().getCurrency(), item.getTags(), item.getAttributes(),
                item.getBranch() == null ? null : item.getBranch().getLatitude(),
                item.getBranch() == null ? null : item.getBranch().getLongitude());
        Long version = searchDocumentService.upsert(projection);
        searchOutboxService.publish(SearchAggregateType.ITEM, item.getId(), SearchEventType.UPSERT, version);
    }

    private void syncServiceProjection(kz.ask.offer.service.domain.entity.Service service) {
        if (!Boolean.TRUE.equals(service.getIsActive())) {
            Long version = searchDocumentService.delete(SearchDocumentType.SERVICE, service.getId());
            searchOutboxService.publish(SearchAggregateType.SERVICE, service.getId(), SearchEventType.DELETE, version);
            return;
        }
        SearchDocumentDto projection = searchProjectionComposer.composeService(
                service.getId(), service.getBusiness().getId(),
                service.getBranch() == null ? null : service.getBranch().getId(),
                service.getName(), service.getDescription(), service.getCategoryLabel(),
                service.getBusiness().getName(),
                service.getBranch() == null ? null : service.getBranch().getName(),
                service.getBasePrice(), service.getBusiness().getCurrency(), service.getAttributes(),
                service.getBranch() == null ? null : service.getBranch().getLatitude(),
                service.getBranch() == null ? null : service.getBranch().getLongitude());
        Long version = searchDocumentService.upsert(projection);
        searchOutboxService.publish(SearchAggregateType.SERVICE, service.getId(), SearchEventType.UPSERT, version);
    }

    private Map<UUID, DeepSeekAttributeExtractor.ExtractionResult> extract(
            List<DeepSeekAttributeExtractor.ExtractionItem> items) {
        return extractor.extractBatch(items).stream().collect(java.util.stream.Collectors.toMap(
                DeepSeekAttributeExtractor.ExtractionResult::getId,
                Function.identity()));
    }

    private DeepSeekAttributeExtractor.ExtractionItem toExtractionItem(UUID id, String name, String description,
                                                                         String categoryLabel, List<String> aliases) {
        return DeepSeekAttributeExtractor.ExtractionItem.builder()
                .id(id)
                .title(name)
                .description(description)
                .categoryLabel(categoryLabel)
                .aliases(aliases == null ? "" : aliases.stream()
                        .filter(StringUtils::hasText)
                        .map(String::trim)
                        .collect(java.util.stream.Collectors.joining(", ")))
                .build();
    }

    private void apply(Item item, DeepSeekAttributeExtractor.ExtractionResult result) {
        item.setDescription(fillDescription(item.getDescription(), result.getSearchSummary()));
        item.setTags(mergeTerms(item.getTags(), result.getAliases()));
        item.setAttributes(mergeAttributes(item.getAttributes(), result));
    }

    private void apply(kz.ask.offer.service.domain.entity.Service service,
                       DeepSeekAttributeExtractor.ExtractionResult result) {
        service.setDescription(fillDescription(service.getDescription(), result.getSearchSummary()));
        service.setAttributes(mergeAttributes(service.getAttributes(), result));
    }

    private void apply(UniqueOffer offer, DeepSeekAttributeExtractor.ExtractionResult result) {
        offer.setDescription(fillDescription(offer.getDescription(), result.getSearchSummary()));
        offer.setTags(mergeTerms(offer.getTags(), result.getAliases()));
    }

    private Map<String, Object> mergeAttributes(Map<String, Object> current,
                                                 DeepSeekAttributeExtractor.ExtractionResult result) {
        Map<String, Object> attributes = new HashMap<>(current == null ? Map.of() : current);
        result.getFacts().forEach(fact -> attributes.putIfAbsent(fact.getKey(),
                objectMapper.convertValue(fact.getValue(), Object.class)));
        return attributes;
    }

    private List<String> mergeTerms(List<String> current, List<String> additions) {
        List<String> terms = new ArrayList<>(current == null ? List.of() : current);
        additions.stream().filter(StringUtils::hasText).map(String::trim)
                .filter(term -> terms.stream().noneMatch(term::equalsIgnoreCase)).forEach(terms::add);
        return terms;
    }

    private String fillDescription(String current, String enrichment) {
        return StringUtils.hasText(current) || !StringUtils.hasText(enrichment) ? current : enrichment.trim();
    }

    private List<UUID> toList(Iterable<UUID> ids) {
        List<UUID> values = new ArrayList<>();
        ids.forEach(values::add);
        return values;
    }

    private void requireActiveGrant(UUID platformUserId, UUID businessId, BusinessScope requiredScope) {
        BusinessScope activeScope = managedImportService.activeScope(
                businessId, platformUserId);
        if (activeScope == null
                || requiredScope != null
                && activeScope != BusinessScope.BOTH
                && activeScope != requiredScope) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
    }

    private RequestAiEnrichmentResponse response(Integer enrichedCount) {
        return RequestAiEnrichmentResponse.builder().enrichedCount(enrichedCount).build();
    }
}
