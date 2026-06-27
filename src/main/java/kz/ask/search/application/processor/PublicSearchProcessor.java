package kz.ask.search.application.processor;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import kz.ask.search.api.dto.SearchResultCardResponse;
import kz.ask.search.domain.entity.SearchDocument;
import kz.ask.search.domain.enums.SearchDocumentType;
import kz.ask.search.infrastructure.repository.SearchDocumentRepository;
import kz.ask.search.infrastructure.repository.SearchQueryAliasRepository;
import kz.ask.shared.domain.enums.RecordStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PublicSearchProcessor {

    private static final Integer MAX_PAGE_SIZE = 50;

    private final SearchDocumentRepository searchDocumentRepository;
    private final SearchQueryAliasRepository searchQueryAliasRepository;

    @Transactional(readOnly = true)
    public List<SearchResultCardResponse> search(String query, String scope, String category, Integer page, Integer size) {
        List<SearchDocumentType> documentTypes = resolveDocumentTypes(scope);
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase();
        String normalizedCategory = category == null ? "" : category.trim().toLowerCase();

        Integer safeSize = Math.min(Math.max(size == null ? 20 : size, 1), MAX_PAGE_SIZE);
        Integer safePage = Math.max(page == null ? 0 : page, 0);

        return search(documentTypes, normalizedQuery, normalizedCategory, safePage, safeSize)
                .stream()
                .map(this::toCard)
                .toList();
    }

    private List<SearchDocument> search(List<SearchDocumentType> documentTypes, String normalizedQuery,
                                        String normalizedCategory, Integer safePage, Integer safeSize) {
        List<SearchDocument> documents = new ArrayList<>();
        Set<UUID> documentIds = new LinkedHashSet<>();
        for (String queryTerm : resolveQueryTerms(normalizedQuery)) {
            List<SearchDocument> found = searchDocumentRepository.search(
                    documentTypes, queryTerm, normalizedCategory, PageRequest.of(safePage, safeSize)).getContent();
            for (SearchDocument document : found) {
                if (documentIds.add(document.getId())) {
                    documents.add(document);
                }
                if (documents.size() >= safeSize) {
                    return documents;
                }
            }
        }
        return documents;
    }

    private List<String> resolveQueryTerms(String normalizedQuery) {
        Set<String> queryTerms = new LinkedHashSet<>();
        queryTerms.add(normalizedQuery);
        if (!normalizedQuery.isBlank()) {
            searchQueryAliasRepository.findByAliasValueAndStatus(normalizedQuery, RecordStatus.ACTIVE)
                    .stream()
                    .map(alias -> alias.getTargetQuery().trim().toLowerCase())
                    .filter(term -> !term.isBlank())
                    .forEach(queryTerms::add);
        }
        return queryTerms.stream().toList();
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
                .source(document.getSource() != null ? document.getSource() : "CATALOG")
                .publicNote(document.getPublicNote() != null ? document.getPublicNote() : "")
                .contactActions(document.getDocumentType() == SearchDocumentType.SERVICE
                        ? List.of("CHAT", "MAP", "REQUEST")
                        : List.of("CALL", "MAP", "REQUEST"))
                .build();
    }
}
