package kz.ask.business.domain;

import java.time.Instant;
import java.util.UUID;
import kz.ask.business.domain.dto.BusinessCardDto;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.entity.BusinessCard;
import kz.ask.business.domain.enums.StorefrontPageStatus;
import kz.ask.business.infrastructure.mapper.BusinessMapper;
import kz.ask.business.infrastructure.repository.BusinessCardRepository;
import kz.ask.business.infrastructure.repository.BusinessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BusinessCardServiceImpl implements BusinessCardService {

    private final BusinessCardRepository businessCardRepository;
    private final BusinessRepository businessRepository;
    private final BusinessMapper businessMapper;

    @Override
    public BusinessCardDto findByBusiness(UUID businessId) {
        return businessCardRepository.findByBusinessId(businessId)
                .map(businessMapper::toBusinessCardDto)
                .orElse(null);
    }

    @Override
    @Transactional
    public BusinessCardDto saveDraft(UUID businessId, String blocks) {
        BusinessCard card = businessCardRepository.findByBusinessId(businessId)
                .orElseGet(() -> {
                    Business business = businessRepository.getReferenceById(businessId);
                    BusinessCard newCard = new BusinessCard();
                    newCard.setBusiness(business);
                    newCard.setStatus(StorefrontPageStatus.DRAFT);
                    return newCard;
                });
        card.setBlocks(blocks);
        BusinessCard saved = businessCardRepository.save(card);
        return businessMapper.toBusinessCardDto(saved);
    }

    @Override
    @Transactional
    public BusinessCardDto publish(UUID businessId) {
        BusinessCard card = businessCardRepository.findByBusinessId(businessId)
                .orElseGet(() -> {
                    Business business = businessRepository.getReferenceById(businessId);
                    BusinessCard newCard = new BusinessCard();
                    newCard.setBusiness(business);
                    newCard.setBlocks("[]");
                    return newCard;
                });
        card.setStatus(StorefrontPageStatus.PUBLISHED);
        card.setPublishedAt(Instant.now());
        BusinessCard saved = businessCardRepository.save(card);
        return businessMapper.toBusinessCardDto(saved);
    }
}
