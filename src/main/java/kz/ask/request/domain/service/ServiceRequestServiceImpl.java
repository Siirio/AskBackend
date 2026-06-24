package kz.ask.request.domain.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import kz.ask.request.domain.dto.ActivityDto;
import kz.ask.request.domain.dto.ServiceRequestResult;
import kz.ask.request.domain.entity.CustomerRequest;
import kz.ask.request.domain.entity.RequestTarget;
import kz.ask.request.domain.entity.SupplierResponse;
import kz.ask.request.domain.enums.CustomerRequestStatus;
import kz.ask.request.domain.enums.RequestTargetStatus;
import kz.ask.request.domain.enums.SupplierResponseStatus;
import kz.ask.request.infrastructure.repository.CustomerRequestRepository;
import kz.ask.request.infrastructure.repository.RequestTargetRepository;
import kz.ask.request.infrastructure.repository.SupplierResponseRepository;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServiceRequestServiceImpl implements ServiceRequestService {

    private final RequestTargetRepository requestTargetRepository;
    private final SupplierResponseRepository supplierResponseRepository;
    private final CustomerRequestRepository customerRequestRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ActivityDto> listActivity(UUID branchId) {
        return requestTargetRepository.findByBranch_Id(branchId).stream()
                .map(this::toActivityDto)
                .toList();
    }

    @Override
    @Transactional
    public ServiceRequestResult handleRequest(UUID branchId, UUID customerRequestId,
                                              SupplierResponseStatus status,
                                              Instant proposedStartAt,
                                              Instant confirmedStartAt,
                                              Instant confirmedEndAt,
                                              String providerNote) {
        CustomerRequest request = customerRequestRepository.findById(customerRequestId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.REQUEST_NOT_FOUND));

        RequestTarget target = requestTargetRepository
                .findByCustomerRequest_IdAndBranch_Id(customerRequestId, branchId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.REQUEST_NOT_FOUND));

        SupplierResponse response = new SupplierResponse();
        response.setRequestTarget(target);
        response.setStatus(status);
        response.setComment(providerNote);
        response.setProposedStartAt(proposedStartAt);
        response.setConfirmedStartAt(confirmedStartAt);
        response.setConfirmedEndAt(confirmedEndAt);
        supplierResponseRepository.save(response);

        target.setStatus(RequestTargetStatus.ANSWERED);
        requestTargetRepository.save(target);

        CustomerRequestStatus newRequestStatus = resolveCustomerRequestStatus(status);
        request.setStatus(newRequestStatus);
        customerRequestRepository.save(request);

        boolean shouldCreateBooking = status == SupplierResponseStatus.CAN_PROVIDE
                && confirmedStartAt != null;

        UUID serviceBranchOfferId = request.getServiceBranchOffer() != null
                ? request.getServiceBranchOffer().getId() : null;

        return ServiceRequestResult.builder()
                .requestId(customerRequestId)
                .branchId(branchId)
                .customerRequestStatus(newRequestStatus)
                .supplierResponseStatus(status)
                .requestedStartAt(request.getRequestedStartAt())
                .proposedStartAt(proposedStartAt)
                .confirmedStartAt(confirmedStartAt)
                .confirmedEndAt(confirmedEndAt)
                .providerNote(providerNote)
                .shouldCreateBooking(shouldCreateBooking)
                .customerId(request.getUser().getId())
                .serviceBranchOfferId(serviceBranchOfferId)
                .build();
    }

    private CustomerRequestStatus resolveCustomerRequestStatus(SupplierResponseStatus status) {
        return switch (status) {
            case CAN_PROVIDE, CANNOT_PROVIDE -> CustomerRequestStatus.COMPLETED;
            default -> CustomerRequestStatus.PARTIALLY_RESPONDED;
        };
    }

    private ActivityDto toActivityDto(RequestTarget target) {
        CustomerRequest request = target.getCustomerRequest();
        SupplierResponse latestResponse = supplierResponseRepository
                .findTopByRequestTarget_IdOrderByCreatedAtDesc(target.getId())
                .orElse(null);

        String type = request.getServiceBranchOffer() != null ? "SERVICE" : "PRODUCT";

        return ActivityDto.builder()
                .activityId(target.getId())
                .type(type)
                .requestText(request.getQueryText())
                .branchId(target.getBranch().getId())
                .branchAddress(target.getBranch().getAddress())
                .customerName(request.getUser().getDisplayName())
                .customerContact(request.getUser().getPhone() != null
                        ? request.getUser().getPhone() : request.getUser().getEmail())
                .requestedStartAt(request.getRequestedStartAt())
                .proposedStartAt(latestResponse != null ? latestResponse.getProposedStartAt() : null)
                .confirmedStartAt(latestResponse != null ? latestResponse.getConfirmedStartAt() : null)
                .confirmedEndAt(latestResponse != null ? latestResponse.getConfirmedEndAt() : null)
                .customerRequestStatus(request.getStatus())
                .supplierResponseStatus(latestResponse != null ? latestResponse.getStatus() : null)
                .unreadCount(0)
                .build();
    }
}
