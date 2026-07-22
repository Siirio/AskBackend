package kz.ask.identity.domain;

import java.util.UUID;
import kz.ask.identity.api.dto.CustomerProfileResponse;
import kz.ask.identity.api.dto.UpdateCustomerProfileRequest;

public interface CustomerProfileService {

    CustomerProfileResponse getProfile(UUID userId);

    CustomerProfileResponse updateProfile(UUID userId, UpdateCustomerProfileRequest req);

    CustomerProfileResponse updateIcon(UUID userId, String iconFileId);
}
