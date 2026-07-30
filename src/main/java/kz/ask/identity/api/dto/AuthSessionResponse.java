package kz.ask.identity.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuthSessionResponse {

    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private Instant expiresAt;
    private Boolean isRemembered;
    private Boolean isActivationRequired;
    private String role;
    private String startRoute;
    private AuthUserResponse user;
    private AuthBusinessContextResponse business;
    private List<String> allRoles;
    private Boolean requiresTwoFactor;
    private Boolean isTwoFactorEnabled;
    private UUID verificationId;
    private AuthCustomerProfileResponse customerProfile;
    private List<AuthBusinessMembershipResponse> businessMemberships;
    private AuthPlatformMembershipResponse platformMembership;
    private Integer pendingInvitationsCount;
}
