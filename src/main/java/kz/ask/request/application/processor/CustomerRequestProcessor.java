package kz.ask.request.application.processor;

import java.util.List;
import kz.ask.business.domain.CityService;
import kz.ask.business.domain.dto.CityDto;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.business.infrastructure.repository.BusinessBranchRepository;
import kz.ask.business.infrastructure.repository.CityRepository;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.identity.infrastructure.repository.AppUserRepository;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.request.api.dto.CreateCustomerRequestRequest;
import kz.ask.request.api.dto.CustomerRequestResponse;
import kz.ask.request.domain.entity.CustomerRequest;
import kz.ask.request.domain.entity.RequestTarget;
import kz.ask.request.domain.enums.CustomerRequestStatus;
import kz.ask.request.domain.enums.RequestTargetStatus;
import kz.ask.request.infrastructure.repository.CustomerRequestRepository;
import kz.ask.request.infrastructure.repository.RequestTargetRepository;
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

    @Transactional
    public CustomerRequestResponse createRequest(AskPrincipal principal, CreateCustomerRequestRequest req) {
        AppUser user = appUserRepository.findById(principal.getUserId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        CityDto cityDto = cityService.findOrCreateByName(req.getCityName());

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
}
