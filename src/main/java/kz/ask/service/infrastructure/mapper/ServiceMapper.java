package kz.ask.service.infrastructure.mapper;

import kz.ask.service.domain.dto.ServiceBranchOfferDto;
import kz.ask.service.domain.entity.ServiceBranchOffer;
import org.springframework.stereotype.Component;

@Component
public class ServiceMapper {

    public ServiceBranchOfferDto toDto(ServiceBranchOffer entity) {
        return ServiceBranchOfferDto.builder()
                .serviceOfferingId(entity.getServiceOffering().getId())
                .serviceBranchOfferId(entity.getId())
                .businessId(entity.getServiceOffering().getBusiness().getId())
                .branchId(entity.getBranch().getId())
                .categoryId(entity.getServiceOffering().getCategory().getId())
                .name(entity.getServiceOffering().getName())
                .description(entity.getServiceOffering().getDescription())
                .basePrice(entity.getBasePrice())
                .durationMinutes(entity.getDurationMinutes())
                .scheduleText(entity.getScheduleText())
                .active(entity.getActive())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
