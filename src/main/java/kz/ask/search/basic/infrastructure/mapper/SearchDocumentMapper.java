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
        entity.setAiAttributes(dto.getAiAttributes());
        entity.setAliases(dto.getAliases());
        entity.setConceptIds(dto.getConceptIds());
        entity.setUseCases(dto.getUseCases());
        entity.setSemanticSummary(dto.getSemanticSummary());
        entity.setEmbeddingText(dto.getEmbeddingText());
        entity.setSemanticConfidence(dto.getSemanticConfidence());
        entity.setSemanticEvidence(dto.getSemanticEvidence());
        entity.setSemanticModelVersion(dto.getSemanticModelVersion());
        entity.setSemanticSchemaVersion(dto.getSemanticSchemaVersion());
        entity.setSemanticSourceHash(dto.getSemanticSourceHash());
        entity.setSemanticMetadataSourceHash(dto.getSemanticMetadataSourceHash());
        entity.setSemanticGeneratedAt(dto.getSemanticGeneratedAt());
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
                .price(entity.getPrice())
                .currency(entity.getCurrency())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .city(entity.getBranch() != null && entity.getBranch().getCity() != null
                        ? entity.getBranch().getCity().getName() : null)
                .country(entity.getBranch() != null && entity.getBranch().getCity() != null
                        ? entity.getBranch().getCity().getCountryCode() : null)
                .tokens(entity.getTokens())
                .aliases(entity.getAliases())
                .conceptIds(entity.getConceptIds())
                .useCases(entity.getUseCases())
                .semanticSummary(entity.getSemanticSummary())
                .embeddingText(entity.getEmbeddingText())
                .semanticConfidence(entity.getSemanticConfidence())
                .semanticEvidence(entity.getSemanticEvidence())
                .semanticModelVersion(entity.getSemanticModelVersion())
                .semanticSchemaVersion(entity.getSemanticSchemaVersion())
                .semanticSourceHash(entity.getSemanticSourceHash())
                .semanticMetadataSourceHash(entity.getSemanticMetadataSourceHash())
                .semanticGeneratedAt(entity.getSemanticGeneratedAt())
                .verifiedAttributes(entity.getVerifiedAttributes())
                .aiAttributes(entity.getAiAttributes())
                .source(entity.getSource())
                .availabilityStatus(entity.getAvailabilityStatus())
                .availabilitySource(entity.getAvailabilitySource())
                .projectionVersion(entity.getProjectionVersion())
                .indexedVersion(entity.getIndexedVersion())
                .projectionAction(entity.getProjectionAction())
                .build();
    }
}
