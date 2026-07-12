package kz.ask.search.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import kz.ask.business.infrastructure.repository.BusinessBranchRepository;
import kz.ask.business.infrastructure.repository.BusinessRepository;
import kz.ask.catalog.domain.entity.ProductOffer;
import kz.ask.catalog.infrastructure.repository.ProductOfferRepository;
import kz.ask.search.domain.entity.SearchDocument;
import kz.ask.search.domain.enums.SearchDocumentType;
import kz.ask.search.infrastructure.repository.SearchDocumentRepository;
import kz.ask.service.domain.entity.ServiceBranchOffer;
import kz.ask.service.infrastructure.repository.ServiceBranchOfferRepository;
import kz.ask.shared.domain.enums.RecordStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchDocumentServiceImpl implements SearchDocumentService {

    private final SearchDocumentRepository searchDocumentRepository;
    private final ProductOfferRepository productOfferRepository;
    private final ServiceBranchOfferRepository serviceBranchOfferRepository;
    private final BusinessRepository businessRepository;
    private final BusinessBranchRepository businessBranchRepository;
    private final SearchTermEnricher searchTermEnricher;

    @Override
    @Transactional
    public void syncProductDocument(UUID productOfferId, UUID businessId, UUID branchId,
                                    String title, String summary, String categoryLabel,
                                    String sku, List<String> tags, BigDecimal price, Boolean live) {
        SearchDocument document = searchDocumentRepository.findByProductOfferId(productOfferId)
                .orElseGet(() -> {
                    SearchDocument created = new SearchDocument();
                    created.setDocumentType(SearchDocumentType.PRODUCT);
                    created.setProductOffer(productOfferRepository.getReferenceById(productOfferId));
                    return created;
                });

        document.setTitle(title);
        document.setSummary(summary);
        document.setCategoryLabel(categoryLabel);
        document.setSku(sku);
        document.setBusiness(businessRepository.getReferenceById(businessId));
        document.setBranch(businessBranchRepository.getReferenceById(branchId));
        document.setPrice(price);
        List<String> sourceTerms = new ArrayList<>();
        sourceTerms.add(title);
        sourceTerms.add(summary);
        sourceTerms.add(categoryLabel);
        sourceTerms.add(sku);
        sourceTerms.addAll(tags == null ? List.of() : tags);
        document.setTokens(searchTermEnricher.enrichIndexTerms(sourceTerms));
        document.setStatus(Boolean.TRUE.equals(live) ? RecordStatus.ACTIVE : RecordStatus.ARCHIVED);
        if (document.getSource() == null) {
            document.setSource("CATALOG");
        }

        syncAttributesFromProduct(productOfferId, document);

        searchDocumentRepository.save(document);
    }

    @Override
    @Transactional
    public void syncServiceDocument(UUID serviceBranchOfferId, UUID businessId, UUID branchId,
                                    String title, String summary, String categoryLabel,
                                    BigDecimal price, Boolean live) {
        SearchDocument document = searchDocumentRepository.findByServiceBranchOfferId(serviceBranchOfferId)
                .orElseGet(() -> {
                    SearchDocument created = new SearchDocument();
                    created.setDocumentType(SearchDocumentType.SERVICE);
                    created.setServiceBranchOffer(serviceBranchOfferRepository.getReferenceById(serviceBranchOfferId));
                    return created;
                });

        document.setTitle(title);
        document.setSummary(summary);
        document.setCategoryLabel(categoryLabel);
        document.setBusiness(businessRepository.getReferenceById(businessId));
        document.setBranch(businessBranchRepository.getReferenceById(branchId));
        document.setPrice(price);
        List<String> sourceTerms = new ArrayList<>();
        sourceTerms.add(title);
        sourceTerms.add(summary);
        sourceTerms.add(categoryLabel);
        document.setTokens(searchTermEnricher.enrichIndexTerms(sourceTerms));
        document.setStatus(Boolean.TRUE.equals(live) ? RecordStatus.ACTIVE : RecordStatus.ARCHIVED);
        if (document.getSource() == null) {
            document.setSource("CATALOG");
        }

        syncAttributesFromServiceOffering(serviceBranchOfferId, document);

        searchDocumentRepository.save(document);
    }

    private void syncAttributesFromProduct(UUID productOfferId, SearchDocument document) {
        productOfferRepository.findById(productOfferId)
                .map(ProductOffer::getProduct)
                .ifPresent(product -> {
                    Map<String, Object> attrs = product.getAttributes();
                    if (attrs != null && !attrs.isEmpty()) {
                        document.setAttributes(new HashMap<>(attrs));
                    }
                });
    }

    private void syncAttributesFromServiceOffering(UUID serviceBranchOfferId, SearchDocument document) {
        serviceBranchOfferRepository.findById(serviceBranchOfferId)
                .map(ServiceBranchOffer::getServiceOffering)
                .ifPresent(serviceOffering -> {
                    Map<String, Object> attrs = serviceOffering.getAttributes();
                    if (attrs != null && !attrs.isEmpty()) {
                        document.setAttributes(new HashMap<>(attrs));
                    }
                });
    }

}
