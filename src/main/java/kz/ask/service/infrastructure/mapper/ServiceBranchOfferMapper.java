package kz.ask.service.infrastructure.mapper;

import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.business.domain.entity.Category;
import kz.ask.service.api.dto.BusinessServiceCreateRequest;
import kz.ask.service.api.dto.BusinessServiceUpdateRequest;
import kz.ask.service.application.ServiceBranchOfferDto;
import kz.ask.service.domain.entity.ServiceBranchOffer;
import kz.ask.service.domain.entity.ServiceOffering;
import kz.ask.service.domain.enums.ServiceMode;
import kz.ask.shared.domain.enums.RecordStatus;
import org.springframework.stereotype.Component;

@Component
public class ServiceBranchOfferMapper {

    public ServiceOffering toServiceOfferingEntity(BusinessServiceCreateRequest req, Business businessRef, Category categoryRef) {
        ServiceOffering offering = new ServiceOffering();
        offering.setBusiness(businessRef);
        offering.setCategory(categoryRef);
        offering.setName(req.getName().trim());
        offering.setDescription(req.getDescription());
        offering.setImageUrl(req.getImageUrl());
        offering.setStatus(RecordStatus.ACTIVE);
        return offering;
    }

    public ServiceBranchOffer toBranchOfferEntity(BusinessServiceCreateRequest req, ServiceOffering offering, BusinessBranch branchRef) {
        ServiceBranchOffer offer = new ServiceBranchOffer();
        offer.setServiceOffering(offering);
        offer.setBranch(branchRef);
        offer.setServiceMode(ServiceMode.ON_DEMAND);
        offer.setBasePrice(req.getBasePrice());
        offer.setScheduleText(req.getScheduleText());
        offer.setActive(req.getActive() == null ? Boolean.TRUE : req.getActive());
        offer.setStatus(RecordStatus.ACTIVE);
        return offer;
    }

    public void applyUpdate(BusinessServiceUpdateRequest req, ServiceOffering offering, ServiceBranchOffer offer, Category categoryRefOrNull) {
        if (categoryRefOrNull != null) {
            offering.setCategory(categoryRefOrNull);
        }
        if (req.getName() != null) {
            offering.setName(req.getName().trim());
        }
        if (req.getDescription() != null) {
            offering.setDescription(req.getDescription());
        }
        if (req.getBasePrice() != null) {
            offer.setBasePrice(req.getBasePrice());
        }
        if (req.getScheduleText() != null) {
            offer.setScheduleText(req.getScheduleText());
        }
        if (req.getActive() != null) {
            offer.setActive(req.getActive());
        }
        if (req.getImageUrl() != null) {
            offering.setImageUrl(req.getImageUrl());
        }
    }

    public ServiceBranchOfferDto toDto(ServiceBranchOffer offer) {
        ServiceOffering offering = offer.getServiceOffering();
        Category category = offering.getCategory();
        return ServiceBranchOfferDto.builder()
                .serviceOfferingId(offering.getId())
                .serviceBranchOfferId(offer.getId())
                .searchVersion(offer.getSearchVersion())
                .businessId(offering.getBusiness().getId())
                .branchId(offer.getBranch().getId())
                .categoryId(category != null ? category.getId() : null)
                .categoryLabel(category != null ? category.getName() : null)
                .name(offering.getName())
                .description(offering.getDescription())
                .basePrice(offer.getBasePrice())
                .scheduleText(offer.getScheduleText())
                .active(offer.getActive())
                .status(offer.getStatus().name())
                .imageUrl(offering.getImageUrl())
                .updatedAt(offer.getUpdatedAt())
                .build();
    }
}
