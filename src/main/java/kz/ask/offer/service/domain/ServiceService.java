package kz.ask.offer.service.domain;

import java.util.UUID;
import kz.ask.offer.service.api.dto.BusinessServiceCreateRequest;
import kz.ask.offer.service.api.dto.BusinessServiceUpdateRequest;
import kz.ask.offer.service.application.ServiceOfferingDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ServiceService {

    Page<ServiceOfferingDto> listOffers(UUID businessId, UUID branchId, String categoryName,
                                        Boolean active, String query, Pageable pageable);

    ServiceOfferingDto findById(UUID businessId, UUID serviceOfferingId);

    ServiceOfferingDto createService(UUID businessId, BusinessServiceCreateRequest request);

    ServiceOfferingDto updateService(UUID businessId, UUID serviceOfferingId, BusinessServiceUpdateRequest request);
}
