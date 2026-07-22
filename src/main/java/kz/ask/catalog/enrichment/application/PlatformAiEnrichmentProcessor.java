package kz.ask.catalog.enrichment.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
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
import kz.ask.catalog.enrichment.api.dto.AiEnrichmentTargetType;
import kz.ask.catalog.enrichment.api.dto.RequestAiEnrichmentRequest;
import kz.ask.catalog.enrichment.api.dto.RequestAiEnrichmentResponse;
import kz.ask.ai.infrastructure.client.DeepSeekAttributeExtractor;
import kz.ask.search.basic.domain.SearchOutboxService;
import kz.ask.search.basic.domain.enums.SearchAggregateType;
import kz.ask.search.basic.domain.enums.SearchEventType;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
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
    private final DeepSeekAttributeExtractor extractor;
    private final ObjectMapper objectMapper;

    @Transactional
    public RequestAiEnrichmentResponse request(
            AskPrincipal principal,
            RequestAiEnrichmentRequest request) {
        var membership = platformMembershipService.findActiveByUser(principal.getUserId());
        if (membership == null
                || !membership.getPermissions().contains(Permission.USE_AI_ITEMS_SERVICES_TOOLS)) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
        return switch (request.getTargetType()) {
            case PRODUCT -> enrichProducts(principal.getUserId(), request.getAggregateIds());
            case SERVICE -> enrichServices(principal.getUserId(), request.getAggregateIds());
            case UNIQUE_OFFER -> enrichUniqueOffers(principal.getUserId(), request.getAggregateIds());
        };
    }

    private RequestAiEnrichmentResponse enrichProducts(UUID platformUserId, Iterable<UUID> ids) {
        List<Item> items = productRepository.findByIdIn(toList(ids));
        items.forEach(item -> requireActiveGrant(
                platformUserId, item.getBusiness().getId(), BusinessScope.ITEM));
        if (!extractor.isAvailable()) {
            return response(0);
        }
        Map<UUID, DeepSeekAttributeExtractor.ExtractionResult> results = extract(items.stream()
                .map(item -> toExtractionItem(item.getId(), item.getName(), item.getDescription(),
                        item.getCategoryLabel(), item.getTags()))
                .toList());
        Instant now = Instant.now();
        items.forEach(item -> {
            DeepSeekAttributeExtractor.ExtractionResult result = results.get(item.getId());
            if (result == null) {
                return;
            }
            apply(item, result);
            searchOutboxService.republish(SearchAggregateType.PRODUCT_OFFER, item.getId(),
                    SearchEventType.UPSERT, now.toEpochMilli());
        });
        return response(results.size());
    }

    private RequestAiEnrichmentResponse enrichServices(UUID platformUserId, Iterable<UUID> ids) {
        List<kz.ask.offer.service.domain.entity.Service> services = serviceOfferingRepository.findByIdIn(toList(ids));
        services.forEach(service -> requireActiveGrant(
                platformUserId, service.getBusiness().getId(), BusinessScope.SERVICE));
        if (!extractor.isAvailable()) {
            return response(0);
        }
        Map<UUID, DeepSeekAttributeExtractor.ExtractionResult> results = extract(services.stream()
                .map(service -> toExtractionItem(service.getId(), service.getName(), service.getDescription(),
                        service.getCategoryLabel(), List.of()))
                .toList());
        Instant now = Instant.now();
        services.forEach(service -> {
            DeepSeekAttributeExtractor.ExtractionResult result = results.get(service.getId());
            if (result == null) {
                return;
            }
            apply(service, result);
            searchOutboxService.republish(SearchAggregateType.SERVICE_BRANCH_OFFER, service.getId(),
                    SearchEventType.UPSERT, now.toEpochMilli());
        });
        return response(results.size());
    }

    private RequestAiEnrichmentResponse enrichUniqueOffers(UUID platformUserId, Iterable<UUID> ids) {
        List<UniqueOffer> offers = uniqueOfferRepository.findByIdIn(toList(ids));
        offers.forEach(offer -> requireActiveGrant(platformUserId, offer.getBusiness().getId(), null));
        if (!extractor.isAvailable()) {
            return response(0);
        }
        Map<UUID, DeepSeekAttributeExtractor.ExtractionResult> results = extract(offers.stream()
                .map(offer -> toExtractionItem(offer.getId(), offer.getName(), offer.getDescription(),
                        offer.getType().name(), offer.getTags()))
                .toList());
        offers.forEach(offer -> {
            DeepSeekAttributeExtractor.ExtractionResult result = results.get(offer.getId());
            if (result == null) {
                return;
            }
            apply(offer, result);
        });
        return response(results.size());
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
