package kz.ask.request.application.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import kz.ask.business.domain.CityService;
import kz.ask.business.domain.dto.CityDto;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.business.infrastructure.repository.BusinessBranchRepository;
import kz.ask.business.infrastructure.repository.CityRepository;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.identity.infrastructure.repository.AppUserRepository;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.request.api.dto.CreateCustomerRequestRequest;
import kz.ask.request.api.dto.CustomerRequestDetailResponse;
import kz.ask.request.api.dto.CustomerRequestHistoryItem;
import kz.ask.request.api.dto.CustomerRequestResponse;
import kz.ask.request.domain.entity.CustomerRequest;
import kz.ask.request.domain.entity.RequestTarget;
import kz.ask.request.domain.entity.SupplierResponse;
import kz.ask.request.domain.enums.CustomerRequestStatus;
import kz.ask.request.domain.enums.RequestTargetStatus;
import kz.ask.request.domain.enums.SupplierResponseStatus;
import kz.ask.request.infrastructure.repository.CustomerRequestRepository;
import kz.ask.request.infrastructure.repository.RequestTargetRepository;
import kz.ask.request.infrastructure.repository.SupplierResponseRepository;
import kz.ask.shared.domain.enums.RecordStatus;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CustomerRequestProcessor {

    private final CityService cityService;
    private final CityRepository cityRepository;
    private final AppUserRepository appUserRepository;
    private final BusinessBranchRepository businessBranchRepository;
    private final CustomerRequestRepository customerRequestRepository;
    private final RequestTargetRepository requestTargetRepository;
    private final SupplierResponseRepository supplierResponseRepository;

    @Transactional
    public CustomerRequestResponse createRequest(AskPrincipal principal, CreateCustomerRequestRequest req) {
        AppUser user = appUserRepository.findById(principal.getUserId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        CityDto cityDto = cityService.findByName(req.getCityName());

        CustomerRequest customerRequest = new CustomerRequest();
        customerRequest.setUser(user);
        customerRequest.setCity(cityRepository.getReferenceById(cityDto.getId()));
        customerRequest.setQueryText(req.getQueryText());
        customerRequest.setStatus(CustomerRequestStatus.ROUTED);
        customerRequestRepository.save(customerRequest);

        List<BusinessBranch> branches = businessBranchRepository.findByCityIdAndStatus(cityDto.getId(), RecordStatus.ACTIVE);
        for (BusinessBranch branch : branches) {
            RequestTarget target = new RequestTarget();
            target.setCustomerRequest(customerRequest);
            target.setBranch(branch);
            target.setStatus(RequestTargetStatus.PENDING);
            requestTargetRepository.save(target);
        }

        return CustomerRequestResponse.builder()
                .id(customerRequest.getId())
                .query(customerRequest.getQueryText())
                .scope(req.getScope())
                .city(cityDto.getName())
                .status(branches.isEmpty() ? "draft" : "waiting")
                .matchedSuppliers(branches.size())
                .build();
    }

    @Transactional(readOnly = true)
    public List<CustomerRequestHistoryItem> getHistory(AskPrincipal principal) {
        return customerRequestRepository.findByUserIdOrderByCreatedAtDesc(principal.getUserId()).stream()
                .map(this::toHistoryItem)
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerRequestDetailResponse getDetail(AskPrincipal principal, UUID requestId) {
        CustomerRequest cr = customerRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.REQUEST_NOT_FOUND));

        if (!cr.getUser().getId().equals(principal.getUserId())) {
            throw new NotFoundException(ErrorCode.REQUEST_NOT_FOUND);
        }

        List<RequestTarget> targets = requestTargetRepository.findByCustomerRequestId(requestId);
        List<CustomerRequestDetailResponse.SupplierReplyItem> replies = new ArrayList<>();

        for (RequestTarget target : targets) {
            List<SupplierResponse> responses = supplierResponseRepository
                    .findByRequestTargetIdOrderByCreatedAtDesc(target.getId());
            for (SupplierResponse sr : responses) {
                replies.add(CustomerRequestDetailResponse.SupplierReplyItem.builder()
                        .id(sr.getId())
                        .supplierName(target.getBranch().getBusiness().getName())
                        .branchName(target.getBranch().getName())
                        .status(sr.getStatus().name())
                        .statusLabel(mapSupplierStatusLabel(sr.getStatus()))
                        .price(sr.getPrice())
                        .productHint(sr.getProductHint())
                        .comment(sr.getComment())
                        .createdAt(sr.getCreatedAt())
                        .build());
            }
        }

        return CustomerRequestDetailResponse.builder()
                .id(cr.getId())
                .query(cr.getQueryText())
                .scope(cr.getProductOffer() != null ? "PRODUCT" : "SERVICE")
                .city(cr.getCity().getName())
                .status(cr.getStatus().name())
                .matchedSuppliers(targets.size())
                .createdAt(cr.getCreatedAt())
                .replies(replies)
                .build();
    }

    private CustomerRequestHistoryItem toHistoryItem(CustomerRequest cr) {
        List<RequestTarget> targets = requestTargetRepository.findByCustomerRequestId(cr.getId());
        int replyCount = 0;
        for (RequestTarget target : targets) {
            replyCount += supplierResponseRepository
                    .findByRequestTargetIdOrderByCreatedAtDesc(target.getId()).size();
        }

        return CustomerRequestHistoryItem.builder()
                .id(cr.getId())
                .query(cr.getQueryText())
                .scope(cr.getProductOffer() != null ? "PRODUCT" : "SERVICE")
                .city(cr.getCity().getName())
                .status(cr.getStatus().name())
                .matchedSuppliers(targets.size())
                .replyCount(replyCount)
                .createdAt(cr.getCreatedAt())
                .build();
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
}
