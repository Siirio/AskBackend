package kz.ask.request.application.processor;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import kz.ask.business.domain.BranchMemberService;
import kz.ask.business.domain.BusinessBranchService;
import kz.ask.business.domain.BusinessService;
import kz.ask.business.domain.dto.BusinessBranchDto;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.request.api.dto.SupplierTaskResponse;
import kz.ask.request.domain.entity.CustomerRequest;
import kz.ask.request.domain.entity.RequestTarget;
import kz.ask.request.domain.enums.RequestTargetStatus;
import kz.ask.request.infrastructure.repository.RequestTargetRepository;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import kz.ask.shared.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SupplierTaskProcessor {

    private final BusinessService businessService;
    private final BusinessBranchService businessBranchService;
    private final BranchMemberService branchMemberService;
    private final RequestTargetRepository requestTargetRepository;

    @Transactional(readOnly = true)
    public List<SupplierTaskResponse> listTasks(AskPrincipal principal, UUID branchId) {
        BusinessBranchDto branch = businessBranchService.findById(branchId);
        if (branch == null) {
            throw new NotFoundException(ErrorCode.BRANCH_NOT_FOUND);
        }
        requireAnyAccess(principal.getUserId(), branch);

        return requestTargetRepository.findByBranchIdOrderByCreatedAtDesc(branchId).stream()
                .filter(target -> target.getStatus() != RequestTargetStatus.SKIPPED && target.getStatus() != RequestTargetStatus.EXPIRED)
                .map(this::toResponse)
                .toList();
    }

    private SupplierTaskResponse toResponse(RequestTarget target) {
        CustomerRequest customerRequest = target.getCustomerRequest();
        long ageMinutes = Duration.between(customerRequest.getCreatedAt(), Instant.now()).toMinutes();
        boolean hasOfferHint = customerRequest.getProductOffer() != null || customerRequest.getServiceBranchOffer() != null;

        return SupplierTaskResponse.builder()
                .id(target.getId())
                .query(customerRequest.getQueryText())
                .customerArea(customerRequest.getCity() != null ? customerRequest.getCity().getName() : "")
                .categoryName(customerRequest.getCategory() != null ? customerRequest.getCategory().getName() : "")
                .ageMinutes(Math.max(ageMinutes, 0))
                .confidenceCode(hasOfferHint ? "HIGH" : "MEDIUM")
                .status(mapStatus(target.getStatus()))
                .build();
    }

    private String mapStatus(RequestTargetStatus status) {
        return switch (status) {
            case PENDING -> "NEW";
            case SENT -> "NEEDS_REPLY";
            case ANSWERED -> "ANSWERED";
            default -> "ANSWERED";
        };
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
