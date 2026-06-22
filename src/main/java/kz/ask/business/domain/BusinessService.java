package kz.ask.business.domain;

import java.util.UUID;
import kz.ask.business.domain.dto.BusinessRegistrationResult;

public interface BusinessService {

    BusinessRegistrationResult registerBusiness(UUID ownerId,
                                                String businessName,
                                                String branchName,
                                                UUID branchCityId,
                                                String branchAddress,
                                                Boolean onlineOnly,
                                                String contactEmail,
                                                String contactPhone);

    BusinessRegistrationResult findByOwner(UUID userId);

    Boolean isOwnerOfBusiness(UUID businessId, UUID userId);
}
