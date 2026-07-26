package kz.ask.search.basic.domain;

import java.util.Optional;
import java.util.UUID;
import kz.ask.search.basic.domain.dto.SearchDocumentDto;
import kz.ask.search.basic.domain.entity.SearchDocument;
import kz.ask.search.basic.domain.enums.SearchDocumentType;
import kz.ask.search.basic.domain.enums.SearchProjectionAction;
import kz.ask.search.basic.infrastructure.mapper.SearchDocumentMapper;
import kz.ask.search.basic.infrastructure.repository.SearchDocumentRepository;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.InternalServerException;
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
        validate(dto);
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
        document.setProjectionAction(SearchProjectionAction.INDEX);
        searchDocumentRepository.save(document);
        return version;
    }

    @Override
    @Transactional
    public Long delete(SearchDocumentType documentType, UUID aggregateId) {
        Long version = searchDocumentRepository.nextVersion();
        SearchDocument document = searchDocumentRepository
                .findProjectionByAggregate(documentType, aggregateId)
                .orElseGet(() -> {
                    SearchDocument tombstone = new SearchDocument();
                    tombstone.setDocumentType(documentType);
                    tombstone.setAggregateId(aggregateId);
                    return tombstone;
                });
        document.setProjectionVersion(version);
        document.setProjectionAction(SearchProjectionAction.DELETE);
        searchDocumentRepository.save(document);
        return version;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SearchDocumentDto> findByAggregate(SearchDocumentType type, UUID aggregateId) {
        return searchDocumentRepository
                .findProjectionByAggregate(type, aggregateId)
                .map(searchDocumentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SearchDocumentDto> prepareDelivery(SearchDocumentType type, UUID aggregateId,
                                                       Long eventVersion, SearchProjectionAction action) {
        SearchDocument document = requireProjection(type, aggregateId);
        int comparison = document.getProjectionVersion().compareTo(eventVersion);
        if (comparison > 0) {
            return Optional.empty();
        }
        if (comparison < 0 || document.getProjectionAction() != action) {
            throw new InternalServerException(ErrorCode.SEARCH_PROJECTION_VERSION_INVARIANT);
        }
        return Optional.of(searchDocumentMapper.toDto(document));
    }

    @Override
    @Transactional
    public Optional<SearchDocumentDto> confirmDelivery(SearchDocumentType type, UUID aggregateId,
                                                       Long eventVersion, SearchProjectionAction action) {
        SearchDocument document = requireProjection(type, aggregateId);
        int comparison = document.getProjectionVersion().compareTo(eventVersion);
        if (comparison < 0) {
            throw new InternalServerException(ErrorCode.SEARCH_PROJECTION_VERSION_INVARIANT);
        }
        if (comparison == 0 && document.getProjectionAction() == action) {
            document.setIndexedVersion(eventVersion);
            document.setIndexedAt(java.time.Instant.now());
            return Optional.empty();
        }
        return Optional.of(searchDocumentMapper.toDto(document));
    }

    private SearchDocument requireProjection(SearchDocumentType type, UUID aggregateId) {
        return searchDocumentRepository.findProjectionByAggregate(type, aggregateId)
                .orElseThrow(() -> new InternalServerException(
                        ErrorCode.SEARCH_PROJECTION_VERSION_INVARIANT));
    }

    private void validate(SearchDocumentDto dto) {
        if (dto == null || dto.getDocumentType() == null || dto.getAggregateId() == null
                || dto.getBusinessId() == null || dto.getTitle() == null || dto.getTitle().isBlank()
                || dto.getNormalizedTitle() == null || dto.getNormalizedTitle().isBlank()
                || dto.getCategoryLabel() == null || dto.getCategoryLabel().isBlank()
                || dto.getBusinessName() == null || dto.getBusinessName().isBlank()
                || dto.getCurrency() == null || dto.getCurrency().isBlank()
                || dto.getTokens() == null || dto.getAliases() == null
                || dto.getVerifiedAttributes() == null || dto.getAiAttributes() == null
                || dto.getAvailabilityStatus() == null || dto.getAvailabilitySource() == null) {
            throw new InternalServerException(ErrorCode.SEARCH_PROJECTION_INVALID);
        }
    }
}
