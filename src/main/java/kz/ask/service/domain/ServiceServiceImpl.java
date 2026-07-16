package kz.ask.service.domain;

import java.util.UUID;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.business.domain.entity.Category;
import kz.ask.business.infrastructure.repository.BusinessBranchRepository;
import kz.ask.business.infrastructure.repository.BusinessRepository;
import kz.ask.business.infrastructure.repository.CategoryRepository;
import kz.ask.service.api.dto.BusinessServiceCreateRequest;
import kz.ask.service.api.dto.BusinessServiceUpdateRequest;
import kz.ask.service.application.ServiceBranchOfferDto;
import kz.ask.service.domain.entity.ServiceBranchOffer;
import kz.ask.service.domain.entity.ServiceOffering;
import kz.ask.service.infrastructure.mapper.ServiceBranchOfferMapper;
import kz.ask.service.infrastructure.repository.ServiceBranchOfferRepository;
import kz.ask.service.infrastructure.repository.ServiceOfferingRepository;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.NotFoundException;
import kz.ask.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServiceServiceImpl implements ServiceService {

    private final ServiceOfferingRepository serviceOfferingRepository;
    private final ServiceBranchOfferRepository serviceBranchOfferRepository;
    private final BusinessRepository businessRepository;
    private final BusinessBranchRepository businessBranchRepository;
    private final CategoryRepository categoryRepository;
    private final ServiceBranchOfferMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceBranchOfferDto> listOffers(UUID branchId, UUID categoryId, Boolean active, String query, Pageable pageable) {
        String term = (query == null || query.isBlank()) ? null : "%" + query.trim().toLowerCase() + "%";
        return serviceBranchOfferRepository.search(branchId, categoryId, active, term, pageable)
                .map(mapper::toDto);
    }

    @Override
    @Transactional
    public ServiceBranchOfferDto createService(UUID businessId, UUID branchId, BusinessServiceCreateRequest req) {
        if (req.getName() == null || req.getName().isBlank()) {
            throw new ValidationException(ErrorCode.PRODUCT_NAME_BLANK);
        }

        Business businessRef = businessRepository.getReferenceById(businessId);
        BusinessBranch branchRef = businessBranchRepository.getReferenceById(branchId);
        Category categoryRef = categoryRepository.getReferenceById(req.getCategoryId());

        ServiceOffering offering = serviceOfferingRepository.save(
                mapper.toServiceOfferingEntity(req, businessRef, categoryRef));
        ServiceBranchOffer offer = serviceBranchOfferRepository.save(
                mapper.toBranchOfferEntity(req, offering, branchRef));
        return mapper.toDto(offer);
    }

    @Override
    @Transactional
    public ServiceBranchOfferDto updateService(UUID serviceOfferingId, UUID branchId, BusinessServiceUpdateRequest req) {
        ServiceBranchOffer offer = findOfferOrThrow(serviceOfferingId, branchId);
        ServiceOffering offering = offer.getServiceOffering();

        if (req.getName() != null && req.getName().isBlank()) {
            throw new ValidationException(ErrorCode.PRODUCT_NAME_BLANK);
        }

        Category categoryRef = req.getCategoryId() != null
                ? categoryRepository.getReferenceById(req.getCategoryId())
                : null;
        mapper.applyUpdate(req, offering, offer, categoryRef);
        offer.setSearchVersion(offer.getSearchVersion() + 1);
        return mapper.toDto(offer);
    }

    private ServiceBranchOffer findOfferOrThrow(UUID serviceOfferingId, UUID branchId) {
        return serviceBranchOfferRepository.findByServiceOfferingIdAndBranchId(serviceOfferingId, branchId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
    }
}
