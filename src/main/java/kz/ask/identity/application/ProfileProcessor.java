package kz.ask.identity.application;

import java.util.UUID;
import kz.ask.identity.api.dto.CustomerProfileResponse;
import kz.ask.identity.api.dto.UpdateCustomerProfileRequest;
import kz.ask.identity.domain.CustomerProfileService;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ProfileProcessor {

    private final CustomerProfileService customerProfileService;

    @Transactional(readOnly = true)
    public CustomerProfileResponse getProfile(AskPrincipal principal) {
        return customerProfileService.getProfile(principal.getUserId());
    }

    @Transactional
    public CustomerProfileResponse updateProfile(AskPrincipal principal, UpdateCustomerProfileRequest req) {
        return customerProfileService.updateProfile(principal.getUserId(), req);
    }

    @Transactional
    public CustomerProfileResponse updateIcon(AskPrincipal principal, String iconFileId) {
        return customerProfileService.updateIcon(principal.getUserId(), iconFileId);
    }
}
