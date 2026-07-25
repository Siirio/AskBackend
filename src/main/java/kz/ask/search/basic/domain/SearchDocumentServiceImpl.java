package kz.ask.search.basic.domain;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import kz.ask.business.branch.infrastructure.repository.BusinessBranchRepository;
import kz.ask.business.core.infrastructure.repository.BusinessRepository;
import kz.ask.search.basic.domain.entity.SearchDocument;
import kz.ask.search.basic.domain.enums.SearchDocumentType;
import kz.ask.search.basic.infrastructure.repository.SearchDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchDocumentServiceImpl implements SearchDocumentService {

    private final SearchDocumentRepository searchDocumentRepository;
    private final BusinessRepository businessRepository;
    private final BusinessBranchRepository businessBranchRepository;
    private final SearchDocumentMapper mapper;

    @Override
    @Transactional
    public Long upsertItemProjection(SearchableItemSource source) {
        SearchDocument document = searchDocumentRepository
                .findProjectionByAggregate(SearchDocumentType.ITEM, source.getItemId())
                .orElseGet(() -> {
                    SearchDocument doc = new SearchDocument();
                    doc.setDocumentType(SearchDocumentType.ITEM);
                    doc.setAggregateId(source.getItemId());
                    doc.setBusiness(businessRepository.getReferenceById(source.getBusinessId()));
                    return doc;
                });
        if (source.getBranchId() != null) {
            document.setBranch(businessBranchRepository.getReferenceById(source.getBranchId()));
        } else {
            document.setBranch(null);
        }
        mapper.populateFromItem(document, source);
        document.setProjectionVersion(Instant.now().toEpochMilli());
        return searchDocumentRepository.save(document).getProjectionVersion();
    }

    @Override
    @Transactional
    public void deleteItemProjection(UUID itemId) {
        searchDocumentRepository
                .findProjectionByAggregate(SearchDocumentType.ITEM, itemId)
                .ifPresent(searchDocumentRepository::delete);
    }

    @Override
    @Transactional
    public Long upsertServiceProjection(SearchableServiceSource source) {
        SearchDocument document = searchDocumentRepository
                .findProjectionByAggregate(SearchDocumentType.SERVICE, source.getServiceOfferingId())
                .orElseGet(() -> {
                    SearchDocument doc = new SearchDocument();
                    doc.setDocumentType(SearchDocumentType.SERVICE);
                    doc.setAggregateId(source.getServiceOfferingId());
                    doc.setBusiness(businessRepository.getReferenceById(source.getBusinessId()));
                    return doc;
                });
        if (source.getBranchId() != null) {
            document.setBranch(businessBranchRepository.getReferenceById(source.getBranchId()));
        } else {
            document.setBranch(null);
        }
        SearchableServiceSource enriched = source.getBusinessName() != null ? source : enrichServiceSource(source);
        mapper.populateFromService(document, enriched);
        document.setProjectionVersion(Instant.now().toEpochMilli());
        return searchDocumentRepository.save(document).getProjectionVersion();
    }

    private SearchableServiceSource enrichServiceSource(SearchableServiceSource source) {
        return SearchableServiceSource.builder()
                .serviceOfferingId(source.getServiceOfferingId())
                .businessId(source.getBusinessId())
                .branchId(source.getBranchId())
                .name(source.getName())
                .description(source.getDescription())
                .categoryLabel(source.getCategoryLabel())
                .businessName(businessRepository.findById(source.getBusinessId())
                        .map(b -> b.getName()).orElse(null))
                .branchName(source.getBranchId() != null
                        ? businessBranchRepository.findById(source.getBranchId())
                            .map(b -> b.getName()).orElse(null)
                        : null)
                .basePrice(source.getBasePrice())
                .scheduleText(source.getScheduleText())
                .attributes(source.getAttributes())
                .latitude(source.getLatitude() != null ? source.getLatitude()
                        : source.getBranchId() != null
                            ? businessBranchRepository.findById(source.getBranchId())
                                .map(b -> b.getLatitude()).orElse(null)
                            : null)
                .longitude(source.getLongitude() != null ? source.getLongitude()
                        : source.getBranchId() != null
                            ? businessBranchRepository.findById(source.getBranchId())
                                .map(b -> b.getLongitude()).orElse(null)
                            : null)
                .active(source.isActive())
                .build();
    }

    @Override
    @Transactional
    public void deleteServiceProjection(UUID serviceOfferingId) {
        searchDocumentRepository
                .findProjectionByAggregate(SearchDocumentType.SERVICE, serviceOfferingId)
                .ifPresent(searchDocumentRepository::delete);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SearchDocument> findByAggregate(SearchDocumentType type, UUID aggregateId) {
        return searchDocumentRepository.findProjectionByAggregate(type, aggregateId);
    }
}
