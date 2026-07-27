package kz.ask.search.basic.domain;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import kz.ask.business.branch.domain.BranchOpeningHoursPolicy;
import kz.ask.search.basic.domain.dto.SearchDocumentDto;
import kz.ask.search.basic.domain.dto.SearchFallbackQueryDto;
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
    private final BranchOpeningHoursPolicy branchOpeningHoursPolicy;

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
    public Optional<Long> upsertSemanticMetadataIfVersion(SearchDocumentDto dto, Long expectedVersion) {
        validate(dto);
        SearchDocument document = searchDocumentRepository
                .findProjectionByAggregateForUpdate(dto.getDocumentType(), dto.getAggregateId())
                .orElseThrow(() -> new InternalServerException(
                        ErrorCode.SEARCH_PROJECTION_VERSION_INVARIANT));
        if (!document.getProjectionVersion().equals(expectedVersion)
                || document.getProjectionAction() != SearchProjectionAction.INDEX) {
            return Optional.empty();
        }
        searchDocumentMapper.populateEntity(document, dto);
        Long version = searchDocumentRepository.nextVersion();
        document.setProjectionVersion(version);
        document.setProjectionAction(SearchProjectionAction.INDEX);
        searchDocumentRepository.save(document);
        return Optional.of(version);
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
    public List<SearchDocumentDto> findSearchableByAggregateIds(
            SearchDocumentType type,
            Collection<UUID> aggregateIds) {
        if (aggregateIds == null || aggregateIds.isEmpty()) {
            return List.of();
        }
        Map<UUID, SearchDocumentDto> documents = searchDocumentRepository
                .findAllByDocumentTypeAndAggregateIdIn(List.of(type), aggregateIds).stream()
                .map(searchDocumentMapper::toDto)
                .collect(LinkedHashMap::new,
                        (values, document) -> values.put(document.getAggregateId(), document),
                        LinkedHashMap::putAll);
        return aggregateIds.stream().map(documents::get).filter(java.util.Objects::nonNull).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchDocumentDto> findPostgresCandidates(SearchFallbackQueryDto query) {
        List<UUID> ids = searchDocumentRepository.findPostgresCandidateIds(
                query.getDocumentTypes().stream().map(Enum::name).toList(),
                query.getQuery(),
                query.getCategory(),
                query.getMinPrice(),
                query.getMaxPrice(),
                query.getCity(),
                query.getCountry(),
                query.getRadiusMeters(),
                query.getUserLatitude(),
                query.getUserLongitude(),
                query.getCandidateLimit());
        return hydrateOrdered(ids, query.getOpenNow());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchDocumentDto> findDirtyCandidates(SearchFallbackQueryDto query) {
        List<UUID> ids = searchDocumentRepository.findDirtyCandidateIds(
                query.getDocumentTypes().stream().map(Enum::name).toList(),
                query.getQuery(),
                query.getCategory(),
                query.getMinPrice(),
                query.getMaxPrice(),
                query.getCity(),
                query.getCountry(),
                query.getRadiusMeters(),
                query.getUserLatitude(),
                query.getUserLongitude(),
                query.getCandidateLimit());
        return hydrateOrdered(ids, query.getOpenNow());
    }

    private List<SearchDocumentDto> hydrateOrdered(List<UUID> ids, Boolean openNow) {
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<UUID, SearchDocument> documents = searchDocumentRepository.findAllByIdIn(ids).stream()
                .collect(java.util.stream.Collectors.toMap(SearchDocument::getId, document -> document));
        return ids.stream()
                .map(documents::get)
                .filter(java.util.Objects::nonNull)
                .filter(document -> !Boolean.TRUE.equals(openNow)
                        || isOpen(document))
                .map(searchDocumentMapper::toDto)
                .toList();
    }

    private Boolean isOpen(SearchDocument document) {
        return branchOpeningHoursPolicy.isOpen(document.getBranch());
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
                || dto.getConceptIds() == null || dto.getUseCases() == null
                || dto.getSemanticSummary() == null || dto.getEmbeddingText() == null
                || dto.getEmbeddingText().isBlank() || dto.getSemanticEvidence() == null
                || dto.getSemanticSchemaVersion() == null || dto.getSemanticSchemaVersion().isBlank()
                || dto.getSemanticSourceHash() == null || dto.getSemanticSourceHash().isBlank()
                || dto.getVerifiedAttributes() == null || dto.getAiAttributes() == null
                || dto.getAvailabilityStatus() == null || dto.getAvailabilitySource() == null) {
            throw new InternalServerException(ErrorCode.SEARCH_PROJECTION_INVALID);
        }
    }
}
