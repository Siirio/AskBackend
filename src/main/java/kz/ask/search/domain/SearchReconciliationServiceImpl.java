package kz.ask.search.domain;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.enums.BusinessModerationStatus;
import kz.ask.business.domain.enums.CatalogStatus;
import kz.ask.catalog.domain.entity.ProductOffer;
import kz.ask.catalog.infrastructure.repository.ProductOfferRepository;
import kz.ask.search.domain.dto.SearchReconciliationBatch;
import kz.ask.search.domain.entity.SearchDocument;
import kz.ask.search.domain.enums.SearchAggregateType;
import kz.ask.search.domain.enums.SearchDocumentType;
import kz.ask.search.domain.enums.SearchEventType;
import kz.ask.search.infrastructure.repository.SearchDocumentRepository;
import kz.ask.service.domain.entity.ServiceBranchOffer;
import kz.ask.service.infrastructure.repository.ServiceBranchOfferRepository;
import kz.ask.shared.domain.enums.RecordStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchReconciliationServiceImpl implements SearchReconciliationService {

    private final ProductOfferRepository productOfferRepository;
    private final ServiceBranchOfferRepository serviceBranchOfferRepository;
    private final SearchDocumentRepository searchDocumentRepository;
    private final SearchOutboxService outboxService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SearchReconciliationBatch reconcileProducts(UUID cursor, Integer batchSize, Boolean repair) {
        List<ProductOffer> offers = productOfferRepository.findReconciliationBatch(
                cursor, PageRequest.of(0, batchSize + 1));
        boolean hasMore = offers.size() > batchSize;
        List<ProductOffer> page = hasMore ? offers.subList(0, batchSize) : offers;
        Map<UUID, SearchDocument> documents = documents(SearchDocumentType.PRODUCT,
                page.stream().map(ProductOffer::getId).toList());
        int mismatches = 0;
        int repairs = 0;
        for (ProductOffer offer : page) {
            RecordStatus expected = isLive(offer) ? RecordStatus.ACTIVE : RecordStatus.ARCHIVED;
            if (matches(documents.get(offer.getId()), offer.getSearchVersion(), expected)) {
                continue;
            }
            mismatches++;
            if (Boolean.TRUE.equals(repair)) {
                outboxService.republish(
                        SearchAggregateType.PRODUCT_OFFER,
                        offer.getId(),
                        expected == RecordStatus.ACTIVE ? SearchEventType.UPSERT : SearchEventType.DELETE,
                        offer.getSearchVersion());
                repairs++;
            }
        }
        return result(page.isEmpty() ? cursor : page.get(page.size() - 1).getId(), hasMore,
                page.size(), mismatches, repairs);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SearchReconciliationBatch reconcileServices(UUID cursor, Integer batchSize, Boolean repair) {
        List<ServiceBranchOffer> offers = serviceBranchOfferRepository.findReconciliationBatch(
                cursor, PageRequest.of(0, batchSize + 1));
        boolean hasMore = offers.size() > batchSize;
        List<ServiceBranchOffer> page = hasMore ? offers.subList(0, batchSize) : offers;
        Map<UUID, SearchDocument> documents = documents(SearchDocumentType.SERVICE,
                page.stream().map(ServiceBranchOffer::getId).toList());
        int mismatches = 0;
        int repairs = 0;
        for (ServiceBranchOffer offer : page) {
            RecordStatus expected = isLive(offer) ? RecordStatus.ACTIVE : RecordStatus.ARCHIVED;
            if (matches(documents.get(offer.getId()), offer.getSearchVersion(), expected)) {
                continue;
            }
            mismatches++;
            if (Boolean.TRUE.equals(repair)) {
                outboxService.republish(
                        SearchAggregateType.SERVICE_BRANCH_OFFER,
                        offer.getId(),
                        expected == RecordStatus.ACTIVE ? SearchEventType.UPSERT : SearchEventType.DELETE,
                        offer.getSearchVersion());
                repairs++;
            }
        }
        return result(page.isEmpty() ? cursor : page.get(page.size() - 1).getId(), hasMore,
                page.size(), mismatches, repairs);
    }

    @Override
    @Transactional(readOnly = true)
    public Long activeProjectionCount() {
        return searchDocumentRepository.countByStatus(RecordStatus.ACTIVE);
    }

    private Map<UUID, SearchDocument> documents(SearchDocumentType type, Collection<UUID> aggregateIds) {
        if (aggregateIds.isEmpty()) {
            return Map.of();
        }
        return searchDocumentRepository.findByDocumentTypeAndAggregateIdIn(type, aggregateIds).stream()
                .collect(Collectors.toMap(SearchDocument::getAggregateId, Function.identity()));
    }

    private boolean matches(SearchDocument document, Long version, RecordStatus expectedStatus) {
        return document != null
                && document.getDocumentVersion().equals(version)
                && document.getStatus() == expectedStatus
                && (expectedStatus != RecordStatus.ACTIVE || document.getIndexedAt() != null);
    }

    private boolean isLive(ProductOffer offer) {
        Business business = offer.getProduct().getBusiness();
        return Boolean.TRUE.equals(offer.getEnabled())
                && offer.getStatus() == RecordStatus.ACTIVE
                && offer.getProduct().getStatus() == RecordStatus.ACTIVE
                && business.getStatus() == RecordStatus.ACTIVE
                && business.getModerationStatus() == BusinessModerationStatus.VISIBLE
                && business.getCatalogStatus() != CatalogStatus.RESTRICTED
                && offer.getBranch().getStatus() == RecordStatus.ACTIVE;
    }

    private boolean isLive(ServiceBranchOffer offer) {
        Business business = offer.getServiceOffering().getBusiness();
        return Boolean.TRUE.equals(offer.getActive())
                && offer.getStatus() == RecordStatus.ACTIVE
                && offer.getServiceOffering().getStatus() == RecordStatus.ACTIVE
                && business.getStatus() == RecordStatus.ACTIVE
                && business.getModerationStatus() == BusinessModerationStatus.VISIBLE
                && business.getCatalogStatus() != CatalogStatus.RESTRICTED
                && offer.getBranch().getStatus() == RecordStatus.ACTIVE;
    }

    private SearchReconciliationBatch result(UUID cursor, boolean hasMore, int scanned,
                                              int mismatches, int repairs) {
        return SearchReconciliationBatch.builder()
                .nextCursor(cursor)
                .hasMore(hasMore)
                .scanned(scanned)
                .mismatches(mismatches)
                .repairsQueued(repairs)
                .build();
    }
}
