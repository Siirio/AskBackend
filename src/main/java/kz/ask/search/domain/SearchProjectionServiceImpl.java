package kz.ask.search.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.enums.BusinessModerationStatus;
import kz.ask.business.domain.enums.CatalogStatus;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.catalog.domain.entity.Product;
import kz.ask.catalog.domain.entity.ProductOffer;
import kz.ask.catalog.infrastructure.repository.ProductOfferRepository;
import kz.ask.search.domain.dto.SearchOutboxEventDto;
import kz.ask.search.domain.dto.SearchProjectionResult;
import kz.ask.search.domain.entity.SearchDocument;
import kz.ask.search.domain.enums.SearchAvailabilitySource;
import kz.ask.search.domain.enums.SearchAvailabilityStatus;
import kz.ask.search.domain.enums.SearchDocumentType;
import kz.ask.search.domain.enums.SearchProjectionAction;
import kz.ask.search.infrastructure.repository.SearchDocumentRepository;
import kz.ask.service.domain.entity.ServiceBranchOffer;
import kz.ask.service.domain.entity.ServiceOffering;
import kz.ask.service.infrastructure.repository.ServiceBranchOfferRepository;
import kz.ask.shared.domain.enums.RecordStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchProjectionServiceImpl implements SearchProjectionService {

    private static final String PROJECTION_SOURCE = "CATALOG";
    private static final String DEFAULT_CURRENCY = "KZT";

    private final ProductOfferRepository productOfferRepository;
    private final ServiceBranchOfferRepository serviceBranchOfferRepository;
    private final SearchDocumentRepository searchDocumentRepository;
    private final SearchTermEnricher searchTermEnricher;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SearchProjectionResult apply(SearchOutboxEventDto event) {
        return switch (event.getAggregateType()) {
            case PRODUCT_OFFER -> applyProduct(event);
            case SERVICE_BRANCH_OFFER -> applyService(event);
        };
    }

    private SearchProjectionResult applyProduct(SearchOutboxEventDto event) {
        ProductOffer offer = productOfferRepository.findProjectionSourceById(event.getAggregateId()).orElse(null);
        if (offer == null) {
            return archiveMissing(SearchDocumentType.PRODUCT, event);
        }
        if (!offer.getSearchVersion().equals(event.getAggregateVersion())) {
            return stale(SearchDocumentType.PRODUCT, event);
        }

        SearchDocument document = findOrCreate(SearchDocumentType.PRODUCT, event.getAggregateId());
        if (document.getDocumentVersion() > event.getAggregateVersion()) {
            return stale(SearchDocumentType.PRODUCT, event);
        }

        Product product = offer.getProduct();
        Business business = product.getBusiness();
        BusinessBranch branch = offer.getBranch();
        document.setProductOffer(offer);
        document.setServiceBranchOffer(null);
        document.setTitle(product.getName());
        document.setNormalizedTitle(normalize(product.getName()));
        document.setSummary(product.getDescription());
        document.setCategoryLabel(product.getCategory() == null ? product.getCategoryLabel() : product.getCategory().getName());
        document.setCategoryPath(document.getCategoryLabel());
        document.setSku(product.getSku());
        document.setCharacteristicsJson(product.getCharacteristicsJson());
        document.setBusiness(business);
        document.setBusinessName(business.getName());
        document.setBranch(branch);
        document.setBranchName(branch.getName());
        document.setPrice(offer.getPrice());
        document.setCurrency(resolveCurrency(business));
        document.setLatitude(branch.getLatitude());
        document.setLongitude(branch.getLongitude());
        document.setAliases(String.join(" ", product.getTags()));
        document.setTokens(indexTerms(product.getName(), product.getDescription(), document.getCategoryLabel(),
                product.getSku(), document.getAliases(), business.getName(), branch.getName()));
        document.setVerifiedAttributes(copy(product.getAttributes()));
        document.setStatus(isLive(offer) ? RecordStatus.ACTIVE : RecordStatus.ARCHIVED);
        prepareForVersion(document, event.getAggregateVersion());
        document.setLastBusinessUpdatedAt(latest(product.getUpdatedAt(), offer.getUpdatedAt()));
        document.setSource(PROJECTION_SOURCE);
        document.setAvailabilityStatus(SearchAvailabilityStatus.UNKNOWN);
        document.setAvailabilitySource(SearchAvailabilitySource.UNKNOWN);
        SearchDocument saved = searchDocumentRepository.save(document);
        return result(saved.getStatus() == RecordStatus.ACTIVE ? SearchProjectionAction.INDEX : SearchProjectionAction.DELETE, saved);
    }

    private SearchProjectionResult applyService(SearchOutboxEventDto event) {
        ServiceBranchOffer offer = serviceBranchOfferRepository.findProjectionSourceById(event.getAggregateId()).orElse(null);
        if (offer == null) {
            return archiveMissing(SearchDocumentType.SERVICE, event);
        }
        if (!offer.getSearchVersion().equals(event.getAggregateVersion())) {
            return stale(SearchDocumentType.SERVICE, event);
        }

        SearchDocument document = findOrCreate(SearchDocumentType.SERVICE, event.getAggregateId());
        if (document.getDocumentVersion() > event.getAggregateVersion()) {
            return stale(SearchDocumentType.SERVICE, event);
        }

        ServiceOffering offering = offer.getServiceOffering();
        Business business = offering.getBusiness();
        BusinessBranch branch = offer.getBranch();
        document.setProductOffer(null);
        document.setServiceBranchOffer(offer);
        document.setTitle(offering.getName());
        document.setNormalizedTitle(normalize(offering.getName()));
        document.setSummary(join(offering.getDescription(), offer.getScheduleText()));
        document.setCategoryLabel(offering.getCategory().getName());
        document.setCategoryPath(document.getCategoryLabel());
        document.setSku(null);
        document.setCharacteristicsJson(null);
        document.setBusiness(business);
        document.setBusinessName(business.getName());
        document.setBranch(branch);
        document.setBranchName(branch.getName());
        document.setPrice(offer.getBasePrice());
        document.setCurrency(resolveCurrency(business));
        document.setLatitude(branch.getLatitude());
        document.setLongitude(branch.getLongitude());
        document.setAliases("");
        document.setTokens(indexTerms(offering.getName(), offering.getDescription(), offer.getScheduleText(),
                document.getCategoryLabel(), business.getName(), branch.getName()));
        document.setVerifiedAttributes(copy(offering.getAttributes()));
        document.setStatus(isLive(offer) ? RecordStatus.ACTIVE : RecordStatus.ARCHIVED);
        prepareForVersion(document, event.getAggregateVersion());
        document.setLastBusinessUpdatedAt(latest(offering.getUpdatedAt(), offer.getUpdatedAt()));
        document.setSource(PROJECTION_SOURCE);
        document.setAvailabilityStatus(SearchAvailabilityStatus.UNKNOWN);
        document.setAvailabilitySource(SearchAvailabilitySource.UNKNOWN);
        SearchDocument saved = searchDocumentRepository.save(document);
        return result(saved.getStatus() == RecordStatus.ACTIVE ? SearchProjectionAction.INDEX : SearchProjectionAction.DELETE, saved);
    }

    private SearchProjectionResult archiveMissing(SearchDocumentType type, SearchOutboxEventDto event) {
        SearchDocument document = searchDocumentRepository.findProjectionByAggregate(type, event.getAggregateId()).orElse(null);
        if (document == null) {
            return SearchProjectionResult.builder()
                    .action(SearchProjectionAction.STALE)
                    .documentType(type)
                    .aggregateId(event.getAggregateId())
                    .documentVersion(event.getAggregateVersion())
                    .build();
        }
        if (document.getDocumentVersion() > event.getAggregateVersion()) {
            return stale(type, event);
        }
        document.setStatus(RecordStatus.ARCHIVED);
        document.setDocumentVersion(event.getAggregateVersion());
        return result(SearchProjectionAction.DELETE, searchDocumentRepository.save(document));
    }

    private SearchDocument findOrCreate(SearchDocumentType type, UUID aggregateId) {
        return searchDocumentRepository.findProjectionByAggregate(type, aggregateId)
                .orElseGet(() -> {
                    SearchDocument document = new SearchDocument();
                    document.setDocumentType(type);
                    document.setAggregateId(aggregateId);
                    return document;
                });
    }

    private SearchProjectionResult stale(SearchDocumentType type, SearchOutboxEventDto event) {
        return SearchProjectionResult.builder()
                .action(SearchProjectionAction.STALE)
                .documentType(type)
                .aggregateId(event.getAggregateId())
                .documentVersion(event.getAggregateVersion())
                .build();
    }

    private SearchProjectionResult result(SearchProjectionAction action, SearchDocument document) {
        return SearchProjectionResult.builder()
                .action(action)
                .documentType(document.getDocumentType())
                .aggregateId(document.getAggregateId())
                .documentId(document.getId())
                .documentVersion(document.getDocumentVersion())
                .build();
    }

    private void prepareForVersion(SearchDocument document, Long version) {
        if (!version.equals(document.getDocumentVersion())) {
            document.setAiEnrichmentAvailableAt(Instant.now());
            document.setAiEnrichmentStartedAt(null);
            document.setAiEnrichmentWorkerId(null);
            document.setAiEnrichmentAttemptCount(0);
            document.setAiEnrichmentError(null);
            document.setAiEnrichmentDead(Boolean.FALSE);
            document.setAiEnrichmentRequested(Boolean.FALSE);
        }
        document.setDocumentVersion(version);
    }

    private boolean isLive(ProductOffer offer) {
        Business business = offer.getProduct().getBusiness();
        return Boolean.TRUE.equals(offer.getEnabled())
                && offer.getStatus() == RecordStatus.ACTIVE
                && offer.getProduct().getStatus() == RecordStatus.ACTIVE
                && !Boolean.TRUE.equals(offer.getProduct().getHiddenByModerator())
                && business.getStatus() == RecordStatus.ACTIVE
                && business.getModerationStatus() == BusinessModerationStatus.VISIBLE
                && business.getCatalogStatus() == CatalogStatus.COMPLETED
                && offer.getBranch().getStatus() == RecordStatus.ACTIVE;
    }

    private boolean isLive(ServiceBranchOffer offer) {
        Business business = offer.getServiceOffering().getBusiness();
        return Boolean.TRUE.equals(offer.getActive())
                && offer.getStatus() == RecordStatus.ACTIVE
                && offer.getServiceOffering().getStatus() == RecordStatus.ACTIVE
                && business.getStatus() == RecordStatus.ACTIVE
                && business.getModerationStatus() == BusinessModerationStatus.VISIBLE
                && business.getCatalogStatus() == CatalogStatus.COMPLETED
                && offer.getBranch().getStatus() == RecordStatus.ACTIVE;
    }

    private List<String> indexTerms(String... values) {
        List<String> terms = new ArrayList<>();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                terms.add(value);
            }
        }
        return searchTermEnricher.enrichIndexTerms(terms);
    }

    private Map<String, Object> copy(Map<String, Object> value) {
        return value == null ? new HashMap<>() : new HashMap<>(value);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String join(String first, String second) {
        return String.join(" ", first == null ? "" : first, second == null ? "" : second).trim();
    }

    private String resolveCurrency(Business business) {
        return business.getCurrency() == null || business.getCurrency().isBlank()
                ? DEFAULT_CURRENCY
                : business.getCurrency();
    }

    private Instant latest(Instant first, Instant second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        return first.isAfter(second) ? first : second;
    }
}
