package kz.ask.service.domain;

import java.util.UUID;
import kz.ask.service.api.dto.BusinessServiceCreateRequest;
import kz.ask.service.api.dto.BusinessServiceUpdateRequest;
import kz.ask.service.application.ServiceBranchOfferDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ServiceService {

    Page<ServiceBranchOfferDto> listOffers(UUID branchId, UUID categoryId, Boolean active, String query, Pageable pageable);

    ServiceBranchOfferDto createService(UUID businessId, UUID branchId, BusinessServiceCreateRequest req);

    ServiceBranchOfferDto updateService(UUID serviceOfferingId, UUID branchId, BusinessServiceUpdateRequest req);
}
