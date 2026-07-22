package kz.ask.offer.service.domain;

import java.util.UUID;

import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.business.infrastructure.repository.BusinessBranchRepository;
import kz.ask.business.infrastructure.repository.BusinessRepository;
import kz.ask.offer.service.api.dto.BusinessServiceCreateRequest;
import kz.ask.offer.service.api.dto.BusinessServiceUpdateRequest;
import kz.ask.offer.service.application.ServiceOfferingDto;
import kz.ask.offer.service.domain.entity.Service;
import kz.ask.offer.service.infrastructure.repository.ServiceOfferingRepository;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.NotFoundException;
import kz.ask.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceServiceImpl implements ServiceService {

    private final ServiceOfferingRepository serviceOfferingRepository;
    private final BusinessRepository businessRepository;
    private final BusinessBranchRepository businessBranchRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceOfferingDto> listOffers(UUID branchId, String categoryLabel, Boolean active, String query, Pageable pageable) {
        String term = (query == null || query.isBlank()) ? null : "%" + query.trim().toLowerCase() + "%";
        return serviceOfferingRepository.search(branchId, categoryLabel, active, term, pageable)
                .map(this::toDto);
    }

    @Override
    @Transactional
    public ServiceOfferingDto createService(UUID businessId, UUID branchId, BusinessServiceCreateRequest req) {
        if (req.getName() == null || req.getName().isBlank()) {
            throw new ValidationException(ErrorCode.PRODUCT_NAME_BLANK);
        }

        Business businessRef = businessRepository.getReferenceById(businessId);
        BusinessBranch branchRef = branchId != null ? businessBranchRepository.getReferenceById(branchId) : null;

        Service offering = new Service();
        offering.setBusiness(businessRef);
        offering.setBranch(branchRef);
        applyCreateFields(offering, req);

        return toDto(serviceOfferingRepository.save(offering));
    }

    @Override
    @Transactional
    public ServiceOfferingDto updateService(UUID serviceOfferingId, BusinessServiceUpdateRequest req) {
        Service offering = serviceOfferingRepository.findById(serviceOfferingId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        if (req.getName() != null && req.getName().isBlank()) {
            throw new ValidationException(ErrorCode.PRODUCT_NAME_BLANK);
        }

        applyUpdateFields(offering, req);
        return toDto(serviceOfferingRepository.save(offering));
    }

    private void applyCreateFields(Service offering, BusinessServiceCreateRequest req) {
        offering.setCategoryLabel(normalizeLabel(req.getCategoryLabel()));
        offering.setName(req.getName().trim());
        offering.setDescription(req.getDescription());
        offering.setServiceMode(req.getServiceMode());
        offering.setBasePrice(req.getBasePrice());
        offering.setScheduleText(req.getScheduleText());
        offering.setActive(req.getActive() != null ? req.getActive() : Boolean.TRUE);
        offering.setAttributes(req.getAttributes());
    }

    private void applyUpdateFields(Service offering, BusinessServiceUpdateRequest req) {
        if (req.getCategoryLabel() != null) {
            offering.setCategoryLabel(normalizeLabel(req.getCategoryLabel()));
        }
        if (req.getName() != null) {
            offering.setName(req.getName().trim());
        }
        if (req.getDescription() != null) {
            offering.setDescription(req.getDescription());
        }
        if (req.getServiceMode() != null) {
            offering.setServiceMode(req.getServiceMode());
        }
        if (req.getBasePrice() != null) {
            offering.setBasePrice(req.getBasePrice());
        }
        if (req.getScheduleText() != null) {
            offering.setScheduleText(req.getScheduleText());
        }
        if (req.getActive() != null) {
            offering.setActive(req.getActive());
        }
        if (req.getAttributes() != null) {
            offering.setAttributes(req.getAttributes());
        }
    }

    private String normalizeLabel(String label) {
        return label == null || label.isBlank() ? null : label.trim();
    }

    private ServiceOfferingDto toDto(Service entity) {
        return ServiceOfferingDto.builder()
                .id(entity.getId())
                .businessId(entity.getBusiness().getId())
                .branchId(entity.getBranch() != null ? entity.getBranch().getId() : null)
                .categoryLabel(entity.getCategoryLabel())
                .name(entity.getName())
                .description(entity.getDescription())
                .serviceMode(entity.getServiceMode())
                .basePrice(entity.getBasePrice())
                .scheduleText(entity.getScheduleText())
                .active(entity.getActive())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
