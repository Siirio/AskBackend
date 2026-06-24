package kz.ask.search.application.processor;

import java.util.List;
import kz.ask.search.api.dto.SearchResultCardResponse;
import kz.ask.search.domain.entity.SearchDocument;
import kz.ask.search.domain.enums.SearchDocumentType;
import kz.ask.search.infrastructure.repository.SearchDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PublicSearchProcessor {

    private static final int MAX_PAGE_SIZE = 50;

    private final SearchDocumentRepository searchDocumentRepository;

    @Transactional(readOnly = true)
    public List<SearchResultCardResponse> search(String query, String scope, Integer page, Integer size) {
        List<SearchDocumentType> documentTypes = resolveDocumentTypes(scope);
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase();

        int safeSize = Math.min(Math.max(size == null ? 20 : size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page == null ? 0 : page, 0);

        return searchDocumentRepository.search(documentTypes, normalizedQuery, PageRequest.of(safePage, safeSize))
                .getContent()
                .stream()
                .map(this::toCard)
                .toList();
    }

    private List<SearchDocumentType> resolveDocumentTypes(String scope) {
        if ("product".equalsIgnoreCase(scope)) {
            return List.of(SearchDocumentType.PRODUCT);
        }
        if ("service".equalsIgnoreCase(scope)) {
            return List.of(SearchDocumentType.SERVICE);
        }
        return List.of(SearchDocumentType.PRODUCT, SearchDocumentType.SERVICE);
    }

    private SearchResultCardResponse toCard(SearchDocument document) {
        return SearchResultCardResponse.builder()
                .id(document.getId())
                .type(document.getDocumentType().name())
                .name(document.getTitle())
                .supplierName(document.getBusiness() != null ? document.getBusiness().getName() : null)
                .branchAddress(document.getBranch() != null ? document.getBranch().getAddress() : null)
                .categoryName(document.getCategoryLabel())
                .priceText(document.getPrice() != null ? "от " + document.getPrice().toBigInteger() + " ₸" : null)
                .confidenceCode(document.getConfidenceCode() != null ? document.getConfidenceCode() : "MEDIUM")
                .source(document.getSource() != null ? document.getSource() : "CATALOG")
                .publicNote(document.getPublicNote() != null ? document.getPublicNote() : "")
                .contactActions(document.getDocumentType() == SearchDocumentType.SERVICE
                        ? List.of("CHAT", "MAP", "REQUEST")
                        : List.of("CALL", "MAP", "REQUEST"))
                .build();
    }
}
