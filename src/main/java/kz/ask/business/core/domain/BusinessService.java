package kz.ask.business.core.domain;

import java.util.UUID;
import kz.ask.business.core.domain.enums.BusinessLegalForm;
import kz.ask.business.core.domain.enums.BusinessScope;
import kz.ask.business.core.domain.dto.BusinessRegistrationResult;

public interface BusinessService {

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
                                       String contactEmail);

    BusinessRegistrationResult findByOwner(UUID userId);

    Boolean isOwnerOfBusiness(UUID businessId, UUID userId);

    Boolean isManagerOrAboveOfBusiness(UUID businessId, UUID userId);

    void updateBusiness(UUID businessId, String name);

    BusinessRegistrationResult findByMember(UUID userId);
}
