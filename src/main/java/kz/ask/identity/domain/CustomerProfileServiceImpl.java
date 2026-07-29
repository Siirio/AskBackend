package kz.ask.identity.domain;

import java.util.UUID;
import kz.ask.identity.api.dto.CustomerProfileResponse;
import kz.ask.identity.api.dto.UpdateCustomerProfileRequest;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.identity.domain.entity.CustomerProfile;
import kz.ask.identity.infrastructure.repository.AppUserRepository;
import kz.ask.identity.infrastructure.repository.CustomerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerProfileServiceImpl implements CustomerProfileService {

    private final CustomerProfileRepository customerProfileRepository;
    private final AppUserRepository appUserRepository;

    @Override
    @Transactional(readOnly = true)
    public CustomerProfileResponse getProfile(UUID userId) {
        CustomerProfile profile = customerProfileRepository.findByUserId(userId).orElse(null);
        return toResponse(profile);
    }

    @Override
    @Transactional
    public CustomerProfileResponse updateProfile(UUID userId, UpdateCustomerProfileRequest req) {
        CustomerProfile profile = customerProfileRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultProfile(userId));

        if (req.getDisplayName() != null) {
            profile.getUser().setDisplayName(req.getDisplayName());
        }

        return toResponse(profile);
    }

    @Override
    @Transactional
    public CustomerProfileResponse updateIcon(UUID userId, String iconFileId) {
        CustomerProfile profile = customerProfileRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultProfile(userId));

        profile.setIconFileId(iconFileId);

        return toResponse(profile);
    }

    private CustomerProfile createDefaultProfile(UUID userId) {
        AppUser user = appUserRepository.getReferenceById(userId);
        CustomerProfile profile = new CustomerProfile();
        profile.setUser(user);
        return customerProfileRepository.save(profile);
    }

    private CustomerProfileResponse toResponse(CustomerProfile profile) {
        if (profile == null) {
            return CustomerProfileResponse.builder().build();
        }
        return CustomerProfileResponse.builder()
                .displayName(profile.getUser().getDisplayName())
                .iconFileId(profile.getIconFileId())
                .build();
    }
}
