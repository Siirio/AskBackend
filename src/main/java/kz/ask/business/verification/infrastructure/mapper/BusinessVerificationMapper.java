package kz.ask.business.verification.infrastructure.mapper;

import kz.ask.business.core.domain.entity.Business;
import kz.ask.business.verification.domain.dto.BusinessVerificationDto;
import kz.ask.business.verification.domain.entity.BusinessVerification;
import org.springframework.stereotype.Component;

@Component
public class BusinessVerificationMapper {

    public BusinessVerification toEntity(Business business, BusinessVerificationDto dto) {
        BusinessVerification verification = new BusinessVerification();
        verification.setBusiness(business);
        verification.setStatus(dto.getStatus());
        verification.setTwoGisUrl(dto.getTwoGisUrl());
        verification.setKaspiUrl(dto.getKaspiUrl());
        verification.setOzonUrl(dto.getOzonUrl());
        verification.setWildberriesUrl(dto.getWildberriesUrl());
        verification.setWebsiteUrl(dto.getWebsiteUrl());
        verification.setInstagramUrl(dto.getInstagramUrl());
        verification.setTelegramUrl(dto.getTelegramUrl());
        verification.setPhone(dto.getPhone());
        verification.setCorporateEmail(dto.getCorporateEmail());
        return verification;
    }

    public BusinessVerificationDto toDto(BusinessVerification entity) {
        return BusinessVerificationDto.builder()
                .id(entity.getId())
                .businessId(entity.getBusiness().getId())
                .status(entity.getStatus())
                .twoGisUrl(entity.getTwoGisUrl())
                .kaspiUrl(entity.getKaspiUrl())
                .ozonUrl(entity.getOzonUrl())
                .wildberriesUrl(entity.getWildberriesUrl())
                .websiteUrl(entity.getWebsiteUrl())
                .instagramUrl(entity.getInstagramUrl())
                .telegramUrl(entity.getTelegramUrl())
                .phone(entity.getPhone())
                .corporateEmail(entity.getCorporateEmail())
                .build();
    }
}
