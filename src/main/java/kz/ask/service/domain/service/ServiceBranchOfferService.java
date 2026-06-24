package kz.ask.service.domain.service;

import java.util.UUID;
import kz.ask.service.api.dto.CreateServiceRequest;
import kz.ask.service.api.dto.UpdateServiceRequest;
import kz.ask.service.domain.dto.ServiceBranchOfferDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ServiceBranchOfferService {

    Page<ServiceBranchOfferDto> listOffers(UUID branchId, UUID categoryId, Boolean active,
                                            String query, Pageable pageable);

    ServiceBranchOfferDto createOffer(UUID businessId, UUID branchId, CreateServiceRequest req);

    ServiceBranchOfferDto updateOffer(UUID serviceOfferingId, UUID branchId, UpdateServiceRequest req);
}
