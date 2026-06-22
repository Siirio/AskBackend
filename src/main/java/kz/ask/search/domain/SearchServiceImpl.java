package kz.ask.search.domain;

import java.util.UUID;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.business.infrastructure.repository.BusinessBranchRepository;
import kz.ask.business.infrastructure.repository.BusinessRepository;
import kz.ask.catalog.domain.entity.Product;
import kz.ask.catalog.domain.entity.ProductOffer;
import kz.ask.catalog.infrastructure.repository.ProductOfferRepository;
import kz.ask.catalog.infrastructure.repository.ProductRepository;
import kz.ask.search.domain.entity.SearchDocument;
import kz.ask.search.infrastructure.mapper.SearchMapper;
import kz.ask.search.infrastructure.repository.SearchDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final SearchDocumentRepository searchDocumentRepository;
    private final ProductOfferRepository productOfferRepository;
    private final ProductRepository productRepository;
    private final BusinessRepository businessRepository;
    private final BusinessBranchRepository businessBranchRepository;
    private final SearchMapper searchMapper;

    @Override
    @Transactional
    public void indexProductOffer(UUID offerId, UUID productId, UUID businessId, UUID branchId) {
        ProductOffer offer = productOfferRepository.getReferenceById(offerId);
        Product product = productRepository.getReferenceById(productId);
        Business business = businessRepository.getReferenceById(businessId);
        BusinessBranch branch = businessBranchRepository.getReferenceById(branchId);

        searchDocumentRepository.findByProductOfferId(offer.getId())
            .ifPresent(searchDocumentRepository::delete);

        SearchDocument doc = searchMapper.toSearchDocument(offer, product, business, branch);
        searchDocumentRepository.save(doc);
    }
}
