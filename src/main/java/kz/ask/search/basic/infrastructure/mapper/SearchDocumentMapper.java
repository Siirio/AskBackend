package kz.ask.search.basic.infrastructure.mapper;

import kz.ask.search.basic.domain.dto.SearchDocumentDto;
import kz.ask.search.basic.domain.entity.SearchDocument;
import org.springframework.stereotype.Component;

@Component
public class SearchDocumentMapper {

    public void populateEntity(SearchDocument entity, SearchDocumentDto dto) {
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
        entity.setAiAttributes(dto.getAiAttributes());
        entity.setAliases(dto.getAliases());
        entity.setSource(dto.getSource());
        entity.setAvailabilityStatus(dto.getAvailabilityStatus());
        entity.setAvailabilitySource(dto.getAvailabilitySource());
    }

    public SearchDocumentDto toDto(SearchDocument entity) {
        return SearchDocumentDto.builder()
                .documentType(entity.getDocumentType())
                .aggregateId(entity.getAggregateId())
                .businessId(entity.getBusiness() != null ? entity.getBusiness().getId() : null)
                .branchId(entity.getBranch() != null ? entity.getBranch().getId() : null)
                .title(entity.getTitle())
                .normalizedTitle(entity.getNormalizedTitle())
                .summary(entity.getSummary())
                .categoryLabel(entity.getCategoryLabel())
                .businessName(entity.getBusinessName())
                .branchName(entity.getBranchName())
                .price(entity.getPrice())
                .currency(entity.getCurrency())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .tokens(entity.getTokens())
                .aliases(entity.getAliases())
                .verifiedAttributes(entity.getVerifiedAttributes())
                .aiAttributes(entity.getAiAttributes())
                .source(entity.getSource())
                .availabilityStatus(entity.getAvailabilityStatus())
                .availabilitySource(entity.getAvailabilitySource())
                .build();
    }
}
