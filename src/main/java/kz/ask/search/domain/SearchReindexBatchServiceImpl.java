package kz.ask.search.domain;

import java.util.List;
import java.util.UUID;
import kz.ask.search.domain.dto.MeilisearchIndexDocument;
import kz.ask.search.domain.dto.SearchReindexBatch;
import kz.ask.search.domain.entity.SearchDocument;
import kz.ask.search.infrastructure.mapper.MeilisearchDocumentMapper;
import kz.ask.search.infrastructure.repository.SearchDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchReindexBatchServiceImpl implements SearchReindexBatchService {

    private final SearchDocumentRepository repository;
    private final MeilisearchDocumentMapper mapper;

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public SearchReindexBatch read(UUID cursor, Integer batchSize) {
        List<SearchDocument> documents = repository.findActiveReindexBatch(
                cursor, PageRequest.of(0, batchSize + 1));
        boolean hasMore = documents.size() > batchSize;
        List<SearchDocument> page = hasMore ? documents.subList(0, batchSize) : documents;
        page.forEach(document -> document.getTokens().size());
        List<MeilisearchIndexDocument> indexDocuments = page.stream().map(mapper::toIndexDocument).toList();
        UUID nextCursor = page.isEmpty() ? cursor : page.get(page.size() - 1).getId();
        return SearchReindexBatch.builder()
                .documents(indexDocuments)
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .build();
    }
}
