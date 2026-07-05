package kz.ask.business.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import kz.ask.business.api.dto.CardResponse;
import kz.ask.business.api.dto.SaveCardRequest;
import kz.ask.business.domain.BusinessCardService;
import kz.ask.business.domain.BusinessService;
import kz.ask.business.domain.dto.BusinessCardDto;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import kz.ask.shared.error.InternalServerException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class BusinessCardProcessor {

    private final BusinessCardService businessCardService;
    private final BusinessService businessService;
    private final ObjectMapper objectMapper;

    public CardResponse getCard(AskPrincipal principal, UUID businessId) {
        verifyOwnerAccess(principal.getUserId(), businessId);
        BusinessCardDto dto = businessCardService.findByBusiness(businessId);
        return toCardResponse(businessId, dto);
    }

    @Transactional
    public CardResponse saveDraft(AskPrincipal principal, UUID businessId, SaveCardRequest req) {
        verifyOwnerAccess(principal.getUserId(), businessId);
        String blocksJson = serializeBlocks(req.getBlocks());
        BusinessCardDto dto = businessCardService.saveDraft(businessId, blocksJson);
        return toCardResponse(businessId, dto);
    }

    @Transactional
    public CardResponse publish(AskPrincipal principal, UUID businessId) {
        verifyOwnerAccess(principal.getUserId(), businessId);
        BusinessCardDto dto = businessCardService.publish(businessId);
        return toCardResponse(businessId, dto);
    }

    private void verifyOwnerAccess(UUID userId, UUID businessId) {
        if (!businessService.isOwnerOfBusiness(businessId, userId)) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
    }

    private String serializeBlocks(List<SaveCardRequest.CardBlockDto> blocks) {
        try {
            return objectMapper.writeValueAsString(blocks);
        } catch (JsonProcessingException e) {
            throw new InternalServerException(ErrorCode.INTERNAL_ERROR, e);
        }
    }

    private CardResponse toCardResponse(UUID businessId, BusinessCardDto dto) {
        if (dto == null) {
            return CardResponse.builder()
                    .businessId(businessId)
                    .blocks(List.of())
                    .publishedAt(null)
                    .build();
        }
        List<CardResponse.CardBlockDto> blocks = deserializeBlocks(dto.getBlocks());
        return CardResponse.builder()
                .businessId(dto.getBusinessId())
                .blocks(blocks)
                .publishedAt(dto.getPublishedAt())
                .build();
    }

    private List<CardResponse.CardBlockDto> deserializeBlocks(String blocksJson) {
        try {
            return objectMapper.readValue(blocksJson, new TypeReference<List<CardResponse.CardBlockDto>>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }
}
