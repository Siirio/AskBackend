package kz.ask.offer.service.domain;

import java.util.UUID;
import kz.ask.offer.service.api.dto.BusinessServiceCreateRequest;
import kz.ask.offer.service.api.dto.BusinessServiceUpdateRequest;
import kz.ask.offer.service.application.ServiceOfferingDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ServiceService {

    Page<ServiceOfferingDto> listOffers(UUID branchId, String categoryLabel, Boolean active, String query, Pageable pageable);

    ServiceOfferingDto createService(UUID businessId, UUID branchId, BusinessServiceCreateRequest req);

    ServiceOfferingDto updateService(UUID serviceOfferingId, BusinessServiceUpdateRequest req);
}
