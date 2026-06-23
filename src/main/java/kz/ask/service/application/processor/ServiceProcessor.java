package kz.ask.service.application.processor;

import jakarta.transaction.Transactional;
import kz.ask.business.domain.BusinessService;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.business.domain.entity.Category;
import kz.ask.business.infrastructure.repository.CategoryRepository;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.service.api.dto.*;
import kz.ask.service.domain.entity.Booking;
import kz.ask.service.domain.entity.ServiceBranchOffer;
import kz.ask.service.domain.entity.ServiceOffering;
import kz.ask.service.domain.enums.BookingStatus;
import kz.ask.service.domain.enums.ServiceMode;
import kz.ask.service.infrastructure.mapper.ServiceMapper;
import kz.ask.service.infrastructure.repository.BookingRepository;
import kz.ask.service.infrastructure.repository.ServiceBranchOfferRepository;
import kz.ask.service.infrastructure.repository.ServiceBranchOfferSpecification;
import kz.ask.service.infrastructure.repository.ServiceOfferingRepository;
import kz.ask.shared.domain.enums.RecordStatus;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import kz.ask.shared.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ServiceProcessor {
    private final ServiceOfferingRepository serviceOfferingRepository;
    private final ServiceBranchOfferRepository serviceBranchOfferRepository;
    private final BookingRepository bookingRepository;
    private final CategoryRepository categoryRepository;
    private final BusinessService businessService;
    private final ServiceMapper serviceMapper;

    @Transactional
    public Page<BusinessServiceRowResponse> listServices(
            AskPrincipal principal, UUID branchId, UUID categoryId,
            Boolean active, String query, int page, int size) {
        checkAccess(principal, branchId);

        Specification<ServiceBranchOffer> specification = Specification.
                where(ServiceBranchOfferSpecification.hasBranch(branchId));
        if(categoryId != null){
            specification = specification.and(ServiceBranchOfferSpecification.hasCategory(categoryId));
        }
        if(active != null){
            specification = specification.and(ServiceBranchOfferSpecification.isActive(active));
        }
        if(query != null){
            specification = specification.and(ServiceBranchOfferSpecification.nameContains(query));
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<ServiceBranchOffer> offerPage = serviceBranchOfferRepository.findAll(specification, pageable);
        return offerPage.map(serviceMapper::toBusinessServiceRowResponse);
    }

    @Transactional
    public BusinessServiceRowResponse createService(
            AskPrincipal principal, UUID branchId, CreateServiceRequest request) {
        checkAccess(principal, branchId);

        BusinessBranch branch =  businessService.findBranchById(branchId);
        Category category =  categoryRepository.findById(request.getCategoryId()).orElse(null);
        if(category == null){
            throw new NotFoundException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        ServiceOffering serviceOffering = new ServiceOffering();
        serviceOffering.setBusiness(branch.getBusiness());
        serviceOffering.setCategory(category);
        serviceOffering.setName(request.getName());
        serviceOffering.setDescription(request.getDescription());
        serviceOffering.setStatus(RecordStatus.ACTIVE);
        serviceOfferingRepository.save(serviceOffering);

        ServiceBranchOffer serviceBranchOffer = new ServiceBranchOffer();
        serviceBranchOffer.setServiceOffering(serviceOffering);
        serviceBranchOffer.setBranch(branch);
        serviceBranchOffer.setServiceMode(ServiceMode.SCHEDULED);
        serviceBranchOffer.setBasePrice(request.getBasePrice());
        serviceBranchOffer.setDurationMinutes(request.getDurationMinutes());
        serviceBranchOffer.setScheduleText(request.getScheduleText());
        serviceBranchOffer.setActive(request.getActive() != null ? request.getActive() : true);
        serviceBranchOffer.setStatus(RecordStatus.ACTIVE);
        serviceBranchOfferRepository.save(serviceBranchOffer);

        return serviceMapper.toBusinessServiceRowResponse(serviceBranchOffer);
    }

    @Transactional
    public BusinessServiceRowResponse updateService(
            AskPrincipal principal, UUID branchId,
            UUID serviceOfferingId, UpdateServiceRequest request) {
        checkAccess(principal, branchId);

        ServiceBranchOffer sbo = serviceBranchOfferRepository
                .findByServiceOfferingIdAndBranchId(serviceOfferingId, branchId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.SERVICE_OFFERING_NOT_FOUND));

        if (request.getBasePrice() != null) sbo.setBasePrice(request.getBasePrice());
        if (request.getDurationMinutes() != null) sbo.setDurationMinutes(request.getDurationMinutes());
        if (request.getScheduleText() != null) sbo.setScheduleText(request.getScheduleText());
        if (request.getActive() != null) sbo.setActive(request.getActive());

        ServiceOffering offering = sbo.getServiceOffering();
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new NotFoundException(ErrorCode.CATEGORY_NOT_FOUND));
            offering.setCategory(category);
        }
        if (request.getName() != null) offering.setName(request.getName());
        if (request.getDescription() != null) offering.setDescription(request.getDescription());

        serviceOfferingRepository.save(offering);
        serviceBranchOfferRepository.save(sbo);

        return serviceMapper.toBusinessServiceRowResponse(sbo);
    }

    @Transactional
    public ServiceRequestResponse handleServiceRequest(
            AskPrincipal principal, UUID branchId, UUID requestId, HandleServiceRequestBody body) {
        checkAccess(principal, branchId);

        Booking booking = bookingRepository.findByIdAndBranch_Id(requestId, branchId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.BOOKING_NOT_FOUND));

        switch (body.getAction()) {
            case "CONFIRM" -> {
                booking.setStatus(BookingStatus.CONFIRMED_BY_BUSINESS);
                booking.setConfirmedStartAt(body.getConfirmedStartAt());
                booking.setConfirmedEndAt(body.getConfirmedEndAt());
                booking.setProviderNote(body.getProviderNote());
            }
            case "DECLINE" -> {
                booking.setStatus(BookingStatus.DECLINED_BY_BUSINESS);
                booking.setProviderNote(body.getProviderNote());
            }
            case "SUGGEST_OTHER_TIME" -> {
                booking.setStatus(BookingStatus.SUGGEST_OTHER_TIME_BY_BUSINESS);
                booking.setProviderNote(body.getProviderNote());
            }
            default -> throw new IllegalArgumentException("Unknown action: " + body.getAction());
        }

        bookingRepository.save(booking);

        ServiceRequestResponse response = new ServiceRequestResponse();
        response.setRequestId(booking.getId());
        response.setBranchId(booking.getBranch().getId());
        response.setStatus(booking.getStatus().name());
        response.setConfirmedStartAt(booking.getConfirmedStartAt());
        response.setConfirmedEndAt(booking.getConfirmedEndAt());
        response.setProviderNote(booking.getProviderNote());
        return response;
    }

    private void checkAccess( AskPrincipal principal, UUID branchId){
        if (!businessService.isOwnerOrStaffOfBranch(branchId, principal.getUserId())) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
    }

}
