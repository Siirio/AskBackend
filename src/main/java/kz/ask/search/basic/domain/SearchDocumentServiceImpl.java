package kz.ask.search.basic.domain;

import java.util.Optional;
import java.util.UUID;
import kz.ask.search.basic.domain.dto.SearchDocumentDto;
import kz.ask.search.basic.domain.entity.SearchDocument;
import kz.ask.search.basic.domain.enums.SearchDocumentType;
import kz.ask.search.basic.infrastructure.mapper.SearchDocumentMapper;
import kz.ask.search.basic.infrastructure.repository.SearchDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchDocumentServiceImpl implements SearchDocumentService {

    private final SearchDocumentRepository searchDocumentRepository;
    private final SearchDocumentMapper searchDocumentMapper;

    @Override
    @Transactional
    public Long upsert(SearchDocumentDto dto) {
        SearchDocument document = searchDocumentRepository
                .findProjectionByAggregate(dto.getDocumentType(), dto.getAggregateId())
                .orElseGet(() -> {
                    SearchDocument doc = new SearchDocument();
                    doc.setDocumentType(dto.getDocumentType());
                    doc.setAggregateId(dto.getAggregateId());
                    return doc;
                });
        searchDocumentMapper.populateEntity(document, dto);
        Long version = searchDocumentRepository.nextVersion();
        document.setProjectionVersion(version);
        searchDocumentRepository.save(document);
        return version;
    }

    @Override
    @Transactional
    public Long delete(SearchDocumentType documentType, UUID aggregateId) {
        Long version = searchDocumentRepository.nextVersion();
        searchDocumentRepository
                .findProjectionByAggregate(documentType, aggregateId)
                .ifPresent(searchDocumentRepository::delete);
        return version;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SearchDocumentDto> findByAggregate(SearchDocumentType type, UUID aggregateId) {
        return searchDocumentRepository
                .findProjectionByAggregate(type, aggregateId)
                .map(searchDocumentMapper::toDto);
    }
}
