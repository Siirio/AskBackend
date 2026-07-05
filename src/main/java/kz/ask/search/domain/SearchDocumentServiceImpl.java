package kz.ask.search.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import kz.ask.business.infrastructure.repository.BusinessBranchRepository;
import kz.ask.business.infrastructure.repository.BusinessRepository;
import kz.ask.business.infrastructure.repository.BrandDropRepository;
import kz.ask.catalog.infrastructure.repository.ProductOfferRepository;
import kz.ask.search.domain.entity.SearchDocument;
import kz.ask.search.domain.enums.SearchDocumentType;
import kz.ask.search.infrastructure.repository.SearchDocumentRepository;
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
    private final BrandDropRepository brandDropRepository;
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

        searchDocumentRepository.save(document);
    }

    @Override
    @Transactional
    public void syncDropDocument(UUID dropId, UUID businessId, String title, String summary,
                                 List<String> tags, Boolean live) {
        SearchDocument document = searchDocumentRepository.findByBrandDropId(dropId)
                .orElseGet(() -> {
                    SearchDocument created = new SearchDocument();
                    created.setDocumentType(SearchDocumentType.DROP);
                    created.setBrandDrop(brandDropRepository.getReferenceById(dropId));
                    return created;
                });

        document.setTitle(title);
        document.setSummary(summary);
        document.setCategoryLabel("DROP");
        document.setBusiness(businessRepository.getReferenceById(businessId));
        document.setBranch(null);
        document.setPrice(null);
        List<String> sourceTerms = new ArrayList<>();
        sourceTerms.add(title);
        sourceTerms.add(summary);
        sourceTerms.add("дроп");
        sourceTerms.add("коллекция");
        sourceTerms.addAll(tags == null ? List.of() : tags);
        document.setTokens(searchTermEnricher.enrichIndexTerms(sourceTerms));
        document.setStatus(Boolean.TRUE.equals(live) ? RecordStatus.ACTIVE : RecordStatus.ARCHIVED);
        document.setSource("DROP");

        searchDocumentRepository.save(document);
    }

    @Override
    @Transactional
    public void archiveDropDocument(UUID dropId) {
        searchDocumentRepository.findByBrandDropId(dropId).ifPresent(document -> {
            document.setStatus(RecordStatus.ARCHIVED);
            searchDocumentRepository.save(document);
        });
    }

    @Override
    @Transactional
    public void deleteDropDocument(UUID dropId) {
        searchDocumentRepository.findByBrandDropId(dropId).ifPresent(searchDocumentRepository::delete);
    }
}
