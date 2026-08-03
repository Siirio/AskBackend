package kz.ask.search.basic.infrastructure.mapper;

import kz.ask.search.basic.domain.dto.SearchDocumentDto;
import kz.ask.search.basic.domain.entity.SearchDocument;
import org.springframework.stereotype.Component;

@Component
public class SearchDocumentMapper {

    public void populateEntity(SearchDocument entity, SearchDocumentDto dto) {
        entity.setDocumentType(dto.getDocumentType());
        entity.setAggregateId(dto.getAggregateId());
        entity.setBusinessId(dto.getBusinessId());
        entity.setBranchId(dto.getBranchId());
        entity.setTitle(dto.getTitle());
        entity.setNormalizedTitle(dto.getNormalizedTitle());
        entity.setSummary(dto.getSummary());
        entity.setCategoryLabel(dto.getCategoryLabel());
        entity.setBusinessName(dto.getBusinessName());
        entity.setBranchName(dto.getBranchName());
        entity.setPrice(dto.getPrice());
        entity.setCurrency(dto.getCurrency());
        entity.setLatitude(dto.getLatitude());
        entity.setLongitude(dto.getLongitude());
        entity.setTokens(dto.getTokens());
        entity.setVerifiedAttributes(dto.getVerifiedAttributes());
        entity.setEmbeddingText(dto.getEmbeddingText());
        entity.setSource(dto.getSource());
        entity.setAvailabilityStatus(dto.getAvailabilityStatus());
        entity.setAvailabilitySource(dto.getAvailabilitySource());
        entity.setProjectionAction(dto.getProjectionAction());
    }

    public SearchDocumentDto toDto(SearchDocument entity) {
        return SearchDocumentDto.builder()
                .documentType(entity.getDocumentType())
                .aggregateId(entity.getAggregateId())
                .businessId(entity.getBusinessId())
                .branchId(entity.getBranchId())
                .title(entity.getTitle())
                .normalizedTitle(entity.getNormalizedTitle())
                .summary(entity.getSummary())
                .categoryLabel(entity.getCategoryLabel())
                .businessName(entity.getBusinessName())
                .branchName(entity.getBranchName())
                .branchAddress(entity.getBranch() == null ? null : entity.getBranch().getAddress())
                .price(entity.getPrice())
                .currency(entity.getCurrency())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .city(entity.getBranch() != null && entity.getBranch().getCity() != null
                        ? entity.getBranch().getCity().getName() : null)
                .country(entity.getBranch() != null && entity.getBranch().getCity() != null
                        ? entity.getBranch().getCity().getCountryCode() : null)
                .tokens(entity.getTokens())
                .embeddingText(entity.getEmbeddingText())
                .verifiedAttributes(entity.getVerifiedAttributes())
                .source(entity.getSource())
                .availabilityStatus(entity.getAvailabilityStatus())
                .availabilitySource(entity.getAvailabilitySource())
                .projectionVersion(entity.getProjectionVersion())
                .indexedVersion(entity.getIndexedVersion())
                .projectionAction(entity.getProjectionAction())
                .build();
    }
}
