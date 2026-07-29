package kz.ask.business.core.domain;

import java.util.List;
import java.util.UUID;
import kz.ask.business.core.domain.dto.BusinessDto;
import kz.ask.business.core.domain.enums.BusinessLegalForm;
import kz.ask.business.core.domain.enums.BusinessScope;
import kz.ask.business.core.domain.dto.BusinessRegistrationResult;
import kz.ask.business.profile.domain.enums.DeliveryCoverage;

public interface BusinessService {

    BusinessDto findById(UUID businessId);

    BusinessRegistrationResult registerBusiness(UUID ownerId,
                                                String businessName,
                                                UUID businessCategoryId,
                                                String businessCategoryName,
                                                BusinessScope businessScope,
                                                String branchName,
                                                UUID branchCityId,
                                                String branchAddress,
                                                Boolean onlineOnly,
                                                String contactEmail,
                                                String countryCode);

    BusinessRegistrationResult onboard(UUID ownerId,
                                       String businessName,
                                       UUID businessCategoryId,
                                       String businessCategoryName,
                                       BusinessScope businessScope,
                                       BusinessLegalForm legalForm,
                                       String legalIdentifier,
                                       String legalName,
                                       String countryCode,
                                       String contactEmail,
                                       DeliveryCoverage deliveryCoverage,
                                       List<String> deliveryCities,
                                       Boolean pickupAvailable,
                                       boolean hasPhysicalBranches);

    BusinessRegistrationResult findByOwner(UUID userId);

    Boolean isOwnerOfBusiness(UUID businessId, UUID userId);

    Boolean isManagerOrAboveOfBusiness(UUID businessId, UUID userId);

    void updateBusiness(UUID businessId, String name);

    BusinessRegistrationResult findByMember(UUID userId);
}
