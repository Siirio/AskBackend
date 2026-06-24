package kz.ask.service.domain.service;

import java.util.UUID;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.business.domain.entity.Category;
import kz.ask.business.infrastructure.repository.BusinessBranchRepository;
import kz.ask.business.infrastructure.repository.BusinessRepository;
import kz.ask.business.infrastructure.repository.CategoryRepository;
import kz.ask.service.api.dto.CreateServiceRequest;
import kz.ask.service.api.dto.UpdateServiceRequest;
import kz.ask.service.domain.dto.ServiceBranchOfferDto;
import kz.ask.service.domain.entity.ServiceBranchOffer;
import kz.ask.service.domain.entity.ServiceOffering;
import kz.ask.service.domain.enums.ServiceMode;
import kz.ask.service.infrastructure.mapper.ServiceMapper;
import kz.ask.service.infrastructure.repository.ServiceBranchOfferRepository;
import kz.ask.service.infrastructure.repository.ServiceBranchOfferSpecification;
import kz.ask.service.infrastructure.repository.ServiceOfferingRepository;
import kz.ask.shared.domain.enums.RecordStatus;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServiceBranchOfferServiceImpl implements ServiceBranchOfferService {

    private final ServiceBranchOfferRepository serviceBranchOfferRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;
    private final BusinessBranchRepository businessBranchRepository;
    private final BusinessRepository businessRepository;
    private final CategoryRepository categoryRepository;
    private final ServiceMapper serviceMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceBranchOfferDto> listOffers(UUID branchId, UUID categoryId,
                                                   Boolean active, String query, Pageable pageable) {
        Specification<ServiceBranchOffer> spec =
                Specification.where(ServiceBranchOfferSpecification.hasBranch(branchId));
        if (categoryId != null) {
            spec = spec.and(ServiceBranchOfferSpecification.hasCategory(categoryId));
        }
        if (active != null) {
            spec = spec.and(ServiceBranchOfferSpecification.isActive(active));
        }
        if (query != null) {
            spec = spec.and(ServiceBranchOfferSpecification.nameContains(query));
        }
        return serviceBranchOfferRepository.findAll(spec, pageable).map(serviceMapper::toDto);
    }

    @Override
    @Transactional
    public ServiceBranchOfferDto createOffer(UUID businessId, UUID branchId, CreateServiceRequest req) {
        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.CATEGORY_NOT_FOUND));

        Business business = businessRepository.getReferenceById(businessId);
        BusinessBranch branch = businessBranchRepository.getReferenceById(branchId);

        ServiceOffering offering = new ServiceOffering();
        offering.setBusiness(business);
        offering.setCategory(category);
        offering.setName(req.getName());
        offering.setDescription(req.getDescription());
        offering.setStatus(RecordStatus.ACTIVE);
        serviceOfferingRepository.save(offering);

        ServiceBranchOffer sbo = new ServiceBranchOffer();
        sbo.setServiceOffering(offering);
        sbo.setBranch(branch);
        sbo.setServiceMode(ServiceMode.SCHEDULED);
        sbo.setBasePrice(req.getBasePrice());
        sbo.setDurationMinutes(req.getDurationMinutes());
        sbo.setScheduleText(req.getScheduleText());
        sbo.setActive(req.getActive() != null ? req.getActive() : Boolean.TRUE);
        sbo.setStatus(RecordStatus.ACTIVE);
        serviceBranchOfferRepository.save(sbo);

        return serviceMapper.toDto(sbo);
    }

    @Override
    @Transactional
    public ServiceBranchOfferDto updateOffer(UUID serviceOfferingId, UUID branchId, UpdateServiceRequest req) {
        ServiceBranchOffer sbo = serviceBranchOfferRepository
                .findByServiceOfferingIdAndBranchId(serviceOfferingId, branchId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.SERVICE_OFFERING_NOT_FOUND));

        if (req.getBasePrice() != null) sbo.setBasePrice(req.getBasePrice());
        if (req.getDurationMinutes() != null) sbo.setDurationMinutes(req.getDurationMinutes());
        if (req.getScheduleText() != null) sbo.setScheduleText(req.getScheduleText());
        if (req.getActive() != null) sbo.setActive(req.getActive());

        ServiceOffering offering = sbo.getServiceOffering();
        if (req.getCategoryId() != null) {
            Category category = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new NotFoundException(ErrorCode.CATEGORY_NOT_FOUND));
            offering.setCategory(category);
        }
        if (req.getName() != null) offering.setName(req.getName());
        if (req.getDescription() != null) offering.setDescription(req.getDescription());

        serviceOfferingRepository.save(offering);
        serviceBranchOfferRepository.save(sbo);

        return serviceMapper.toDto(sbo);
    }
}
