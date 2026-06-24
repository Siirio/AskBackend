package kz.ask.search.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import kz.ask.business.infrastructure.repository.BusinessBranchRepository;
import kz.ask.business.infrastructure.repository.BusinessRepository;
import kz.ask.catalog.infrastructure.repository.ProductOfferRepository;
import kz.ask.search.domain.entity.SearchDocument;
import kz.ask.search.domain.enums.SearchDocumentType;
import kz.ask.search.infrastructure.repository.SearchDocumentRepository;
import kz.ask.shared.domain.enums.RecordStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchDocumentServiceImpl implements SearchDocumentService {

    private final SearchDocumentRepository searchDocumentRepository;
    private final ProductOfferRepository productOfferRepository;
    private final BusinessRepository businessRepository;
    private final BusinessBranchRepository businessBranchRepository;

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
        document.setTokens(new ArrayList<>(tags == null ? List.of() : tags));
        document.setStatus(Boolean.TRUE.equals(live) ? RecordStatus.ACTIVE : RecordStatus.ARCHIVED);
        if (document.getConfidenceCode() == null) {
            document.setConfidenceCode("MEDIUM");
        }
        if (document.getSource() == null) {
            document.setSource("CATALOG");
        }

        searchDocumentRepository.save(document);
    }
}
