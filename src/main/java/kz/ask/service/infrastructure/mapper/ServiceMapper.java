package kz.ask.service.infrastructure.mapper;

import kz.ask.service.api.dto.BusinessServiceRowResponse;
import kz.ask.service.domain.entity.ServiceBranchOffer;
import org.springframework.stereotype.Component;

@Component
public class ServiceMapper {

    public BusinessServiceRowResponse toBusinessServiceRowResponse(ServiceBranchOffer serviceBranchOffer){
        BusinessServiceRowResponse businessServiceRowResponse = new BusinessServiceRowResponse();
        businessServiceRowResponse.setServiceOfferingId(serviceBranchOffer.getServiceOffering().getId());
        businessServiceRowResponse.setServiceBranchOfferId(serviceBranchOffer.getId());
        businessServiceRowResponse.setBranchId(serviceBranchOffer.getBranch().getId());
        businessServiceRowResponse.setCategoryId(serviceBranchOffer.getServiceOffering().getCategory().getId());
        businessServiceRowResponse.setName(serviceBranchOffer.getServiceOffering().getName());
        businessServiceRowResponse.setDescription(serviceBranchOffer.getServiceOffering().getDescription());
        businessServiceRowResponse.setBasePrice(serviceBranchOffer.getBasePrice());
        businessServiceRowResponse.setDurationMinutes(serviceBranchOffer.getDurationMinutes());
        businessServiceRowResponse.setScheduleText(serviceBranchOffer.getScheduleText());
        businessServiceRowResponse.setActive(serviceBranchOffer.getActive());
        businessServiceRowResponse.setUpdatedAt(serviceBranchOffer.getUpdatedAt());
        return businessServiceRowResponse;
    }
}
