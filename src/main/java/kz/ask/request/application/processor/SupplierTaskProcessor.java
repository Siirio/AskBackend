package kz.ask.request.application.processor;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import kz.ask.business.domain.BranchMemberService;
import kz.ask.business.domain.BusinessBranchService;
import kz.ask.business.domain.BusinessService;
import kz.ask.business.domain.dto.BusinessBranchDto;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.request.api.dto.SupplierRespondRequest;
import kz.ask.request.api.dto.SupplierTaskDetailResponse;
import kz.ask.request.api.dto.SupplierTaskResponse;
import kz.ask.request.domain.entity.CustomerRequest;
import kz.ask.request.domain.entity.RequestTarget;
import kz.ask.request.domain.entity.SupplierResponse;
import kz.ask.request.domain.enums.RequestTargetStatus;
import kz.ask.request.domain.enums.SupplierResponseSource;
import kz.ask.request.domain.enums.SupplierResponseStatus;
import kz.ask.request.infrastructure.repository.RequestTargetRepository;
import kz.ask.request.infrastructure.repository.SupplierResponseRepository;
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
    private final SupplierResponseRepository supplierResponseRepository;

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

    @Transactional(readOnly = true)
    public SupplierTaskDetailResponse getDetail(AskPrincipal principal, UUID branchId, UUID taskId) {
        BusinessBranchDto branch = businessBranchService.findById(branchId);
        if (branch == null) {
            throw new NotFoundException(ErrorCode.BRANCH_NOT_FOUND);
        }
        requireAnyAccess(principal.getUserId(), branch);

        RequestTarget target = requestTargetRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.REQUEST_NOT_FOUND));

        if (!target.getBranch().getId().equals(branchId)) {
            throw new NotFoundException(ErrorCode.REQUEST_NOT_FOUND);
        }

        CustomerRequest cr = target.getCustomerRequest();
        long ageMinutes = Duration.between(cr.getCreatedAt(), Instant.now()).toMinutes();

        List<SupplierTaskDetailResponse.ResponseMessage> messages = new ArrayList<>();

        messages.add(SupplierTaskDetailResponse.ResponseMessage.builder()
                .id(UUID.randomUUID())
                .role("system")
                .text("Запрос от клиента: " + cr.getQueryText())
                .status("INFO")
                .createdAt(cr.getCreatedAt())
                .build());

        List<SupplierResponse> responses = supplierResponseRepository
                .findByRequestTargetIdOrderByCreatedAtDesc(target.getId());
        for (SupplierResponse sr : responses) {
            StringBuilder text = new StringBuilder();
            if (sr.getProductHint() != null && !sr.getProductHint().isEmpty()) {
                text.append(sr.getProductHint());
            }
            if (sr.getComment() != null && !sr.getComment().isEmpty()) {
                if (!text.isEmpty()) text.append("\n");
                text.append(sr.getComment());
            }
            if (text.isEmpty()) {
                text.append(mapSupplierStatusLabel(sr.getStatus()));
            }

            messages.add(SupplierTaskDetailResponse.ResponseMessage.builder()
                    .id(sr.getId())
                    .role("business")
                    .text(text.toString())
                    .status(sr.getStatus().name())
                    .price(sr.getPrice() != null ? sr.getPrice().toString() : null)
                    .createdAt(sr.getCreatedAt())
                    .build());
        }

        String customerName = cr.getUser().getDisplayName();
        String customerContact = cr.getUser().getEmail();

        return SupplierTaskDetailResponse.builder()
                .id(target.getId())
                .query(cr.getQueryText())
                .scope(cr.getProductOffer() != null ? "PRODUCT" : "SERVICE")
                .customerName(customerName)
                .customerContact(customerContact)
                .city(cr.getCity().getName())
                .categoryName(cr.getCategory() != null ? cr.getCategory().getName() : "")
                .ageMinutes(Math.max(ageMinutes, 0))
                .status(mapStatus(target.getStatus()))
                .createdAt(cr.getCreatedAt())
                .messages(messages)
                .build();
    }

    @Transactional
    public SupplierTaskDetailResponse respond(AskPrincipal principal, UUID branchId, UUID taskId,
                                               SupplierRespondRequest req) {
        BusinessBranchDto branch = businessBranchService.findById(branchId);
        if (branch == null) {
            throw new NotFoundException(ErrorCode.BRANCH_NOT_FOUND);
        }
        requireAnyAccess(principal.getUserId(), branch);

        RequestTarget target = requestTargetRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.REQUEST_NOT_FOUND));

        if (!target.getBranch().getId().equals(branchId)) {
            throw new NotFoundException(ErrorCode.REQUEST_NOT_FOUND);
        }

        SupplierResponseStatus status;
        try {
            status = SupplierResponseStatus.valueOf(req.getStatus());
        } catch (IllegalArgumentException e) {
            status = SupplierResponseStatus.HAS_ITEM;
        }

        SupplierResponse response = new SupplierResponse();
        response.setRequestTarget(target);
        response.setStatus(status);
        response.setResponseSource(resolveResponseSource(status));
        response.setPrice(req.getPrice());
        response.setProductHint(req.getProductHint());
        response.setComment(req.getComment());
        supplierResponseRepository.save(response);

        target.setStatus(RequestTargetStatus.ANSWERED);
        requestTargetRepository.save(target);

        return getDetail(principal, branchId, taskId);
    }

    private SupplierTaskResponse toResponse(RequestTarget target) {
        CustomerRequest customerRequest = target.getCustomerRequest();
        long ageMinutes = Duration.between(customerRequest.getCreatedAt(), Instant.now()).toMinutes();

        return SupplierTaskResponse.builder()
                .id(target.getId())
                .query(customerRequest.getQueryText())
                .customerArea(customerRequest.getCity() != null ? customerRequest.getCity().getName() : "")
                .categoryName(customerRequest.getCategory() != null ? customerRequest.getCategory().getName() : "")
                .ageMinutes(Math.max(ageMinutes, 0))
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

    private String mapSupplierStatusLabel(SupplierResponseStatus status) {
        return switch (status) {
            case HAS_ITEM -> "Есть в наличии";
            case HAS_ANALOG -> "Есть похожий вариант";
            case CAN_PROVIDE -> "Может подтвердить";
            case CANNOT_PROVIDE -> "Не может предоставить";
            case NEED_CLARIFICATION -> "Нужно уточнение";
            case SUGGEST_OTHER_TIME -> "Предлагает другое время";
            case NO_ITEM -> "Нет в наличии";
        };
    }

    private SupplierResponseSource resolveResponseSource(SupplierResponseStatus status) {
        if (status == SupplierResponseStatus.HAS_ITEM || status == SupplierResponseStatus.CAN_PROVIDE) {
            return SupplierResponseSource.BUSINESS_CONFIRMED;
        }
        return SupplierResponseSource.STAFF_REPLY;
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
