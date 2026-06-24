package kz.ask.service.application.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import kz.ask.business.domain.BranchMemberService;
import kz.ask.business.domain.BusinessBranchService;
import kz.ask.business.domain.BusinessService;
import kz.ask.business.domain.dto.BusinessBranchDto;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.request.domain.dto.ActivityDto;
import kz.ask.request.domain.dto.ServiceRequestResult;
import kz.ask.request.domain.enums.SupplierResponseStatus;
import kz.ask.request.domain.service.ServiceRequestService;
import kz.ask.service.api.dto.ActivityDisplayStatus;
import kz.ask.service.api.dto.ActivityRowResponse;
import kz.ask.service.api.dto.BusinessServiceListResponse;
import kz.ask.service.api.dto.BusinessServiceRowResponse;
import kz.ask.service.api.dto.CreateServiceRequest;
import kz.ask.service.api.dto.FixServiceBookingRequest;
import kz.ask.service.api.dto.FixServiceBookingResponse;
import kz.ask.service.api.dto.UpdateServiceRequest;
import kz.ask.service.domain.service.BookingService;
import kz.ask.service.domain.service.ServiceBranchOfferService;
import kz.ask.service.domain.dto.ServiceBranchOfferDto;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import kz.ask.shared.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ServiceProcessor {

    private static final int MAX_PAGE_SIZE = 100;

    private final BusinessService businessService;
    private final BusinessBranchService businessBranchService;
    private final BranchMemberService branchMemberService;
    private final ServiceBranchOfferService serviceBranchOfferService;
    private final ServiceRequestService serviceRequestService;
    private final BookingService bookingService;

    @Transactional(readOnly = true)
    public BusinessServiceListResponse listServices(AskPrincipal principal, UUID branchId,
                                                    UUID categoryId, Boolean active,
                                                    String query, Integer page, Integer size) {
        BusinessBranchDto branch = requireBranch(branchId);
        requireAnyAccess(principal.getUserId(), branch);

        int safeSize = Math.min(Math.max(size == null ? 20 : size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page == null ? 0 : page, 0);

        Page<ServiceBranchOfferDto> offers = serviceBranchOfferService.listOffers(
                branchId, categoryId, active, query, PageRequest.of(safePage, safeSize));

        return BusinessServiceListResponse.builder()
                .items(offers.getContent().stream().map(this::toRowResponse).toList())
                .page(offers.getNumber())
                .size(offers.getSize())
                .totalElements(offers.getTotalElements())
                .totalPages(offers.getTotalPages())
                .build();
    }

    @Transactional
    public BusinessServiceRowResponse createService(AskPrincipal principal, UUID branchId,
                                                    CreateServiceRequest req) {
        BusinessBranchDto branch = requireBranch(branchId);
        requireAnyAccess(principal.getUserId(), branch);

        ServiceBranchOfferDto dto = serviceBranchOfferService.createOffer(branch.getBusinessId(), branchId, req);
        return toRowResponse(dto);
    }

    @Transactional
    public BusinessServiceRowResponse updateService(AskPrincipal principal, UUID branchId,
                                                    UUID serviceOfferingId, UpdateServiceRequest req) {
        BusinessBranchDto branch = requireBranch(branchId);
        requireAnyAccess(principal.getUserId(), branch);

        ServiceBranchOfferDto dto = serviceBranchOfferService.updateOffer(serviceOfferingId, branchId, req);
        return toRowResponse(dto);
    }

    @Transactional(readOnly = true)
    public List<ActivityRowResponse> listActivity(AskPrincipal principal, UUID branchId) {
        BusinessBranchDto branch = requireBranch(branchId);
        requireAnyAccess(principal.getUserId(), branch);

        return serviceRequestService.listActivity(branchId).stream()
                .map(this::toActivityRowResponse)
                .toList();
    }

    @Transactional
    public FixServiceBookingResponse fixServiceBooking(AskPrincipal principal, UUID branchId,
                                                       UUID requestId, FixServiceBookingRequest req) {
        BusinessBranchDto branch = requireBranch(branchId);
        requireAnyAccess(principal.getUserId(), branch);

        ServiceRequestResult result = serviceRequestService.handleRequest(
                branchId, requestId,
                req.getStatus(),
                req.getProposedStartAt(),
                req.getConfirmedStartAt(),
                req.getConfirmedEndAt(),
                req.getProviderNote());

        if (result.isShouldCreateBooking() && result.getServiceBranchOfferId() != null) {
            bookingService.createBookingFromRequest(
                    result.getCustomerId(),
                    result.getServiceBranchOfferId(),
                    branchId,
                    result.getRequestedStartAt(),
                    result.getConfirmedStartAt(),
                    result.getConfirmedEndAt());
        }

        ActivityDisplayStatus displayStatus = deriveDisplayStatus(
                result.getSupplierResponseStatus(),
                result.getConfirmedStartAt());

        return FixServiceBookingResponse.builder()
                .requestId(result.getRequestId())
                .branchId(result.getBranchId())
                .customerRequestStatus(result.getCustomerRequestStatus().name())
                .supplierResponseStatus(result.getSupplierResponseStatus().name())
                .activityDisplayStatus(displayStatus)
                .requestedStartAt(result.getRequestedStartAt())
                .proposedStartAt(result.getProposedStartAt())
                .confirmedStartAt(result.getConfirmedStartAt())
                .confirmedEndAt(result.getConfirmedEndAt())
                .providerNote(result.getProviderNote())
                .build();
    }

    private ActivityRowResponse toActivityRowResponse(ActivityDto dto) {
        ActivityDisplayStatus displayStatus = deriveDisplayStatus(
                dto.getSupplierResponseStatus(),
                dto.getConfirmedStartAt());

        List<String> actions = deriveActions(displayStatus);

        return ActivityRowResponse.builder()
                .activityId(dto.getActivityId())
                .type(dto.getType())
                .requestText(dto.getRequestText())
                .branchId(dto.getBranchId())
                .branchAddress(dto.getBranchAddress())
                .customerName(dto.getCustomerName())
                .customerContact(dto.getCustomerContact())
                .requestedStartAt(dto.getRequestedStartAt())
                .proposedStartAt(dto.getProposedStartAt())
                .confirmedStartAt(dto.getConfirmedStartAt())
                .confirmedEndAt(dto.getConfirmedEndAt())
                .activityDisplayStatus(displayStatus)
                .customerRequestStatus(dto.getCustomerRequestStatus() != null
                        ? dto.getCustomerRequestStatus().name() : null)
                .supplierResponseStatus(dto.getSupplierResponseStatus() != null
                        ? dto.getSupplierResponseStatus().name() : null)
                .unreadCount(dto.getUnreadCount())
                .actions(actions)
                .build();
    }

    private ActivityDisplayStatus deriveDisplayStatus(SupplierResponseStatus responseStatus,
                                                       java.time.Instant confirmedStartAt) {
        if (responseStatus == SupplierResponseStatus.CANNOT_PROVIDE) {
            return ActivityDisplayStatus.CONFIRMATION_DECLINED;
        }
        if (responseStatus == SupplierResponseStatus.CAN_PROVIDE && confirmedStartAt != null) {
            return ActivityDisplayStatus.CONFIRMED;
        }
        return ActivityDisplayStatus.DISCUSSING;
    }

    private List<String> deriveActions(ActivityDisplayStatus displayStatus) {
        List<String> actions = new ArrayList<>();
        actions.add("OPEN_CHAT");
        if (displayStatus == ActivityDisplayStatus.DISCUSSING) {
            actions.add("FIX_BOOKING");
        }
        return actions;
    }

    private BusinessServiceRowResponse toRowResponse(ServiceBranchOfferDto dto) {
        BusinessServiceRowResponse response = new BusinessServiceRowResponse();
        response.setServiceOfferingId(dto.getServiceOfferingId());
        response.setServiceBranchOfferId(dto.getServiceBranchOfferId());
        response.setBranchId(dto.getBranchId());
        response.setCategoryId(dto.getCategoryId());
        response.setName(dto.getName());
        response.setDescription(dto.getDescription());
        response.setBasePrice(dto.getBasePrice());
        response.setDurationMinutes(dto.getDurationMinutes());
        response.setScheduleText(dto.getScheduleText());
        response.setActive(dto.getActive());
        response.setUpdatedAt(dto.getUpdatedAt());
        return response;
    }

    private BusinessBranchDto requireBranch(UUID branchId) {
        BusinessBranchDto branch = businessBranchService.findById(branchId);
        if (branch == null) {
            throw new NotFoundException(ErrorCode.BRANCH_NOT_FOUND);
        }
        return branch;
    }

    private void requireAnyAccess(UUID userId, BusinessBranchDto branch) {
        if (businessService.isOwnerOfBusiness(branch.getBusinessId(), userId)) {
            return;
        }
        if (branchMemberService.isStaffOfBranch(branch.getId(), userId)) {
            return;
        }
        throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
    }
}
