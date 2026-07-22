package kz.ask.identity.application;

import java.util.List;

import kz.ask.business.member.domain.BusinessMemberService;
import kz.ask.business.invitation.domain.BusinessInvitationService;
import kz.ask.identity.api.dto.AuthBusinessMembershipResponse;
import kz.ask.identity.api.dto.AuthCustomerProfileResponse;
import kz.ask.identity.api.dto.AuthPlatformMembershipResponse;
import kz.ask.identity.api.dto.AuthSessionResponse;
import kz.ask.identity.domain.dto.AppUserDto;
import kz.ask.platform.domain.PlatformMembershipService;
import kz.ask.platform.domain.dto.PlatformMembershipDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SessionCapabilitiesProcessor {

    private final BusinessMemberService businessMemberService;

    private final PlatformMembershipService platformMembershipService;
    private final BusinessInvitationService businessInvitationService;

    public void apply(AuthSessionResponse.AuthSessionResponseBuilder builder, AppUserDto user) {
        List<AuthBusinessMembershipResponse> businessMemberships =
                businessMemberService.findActiveByUser(user.getId()).stream()
                        .map(member -> AuthBusinessMembershipResponse.builder()
                                .membershipId(member.getId())
                                .businessId(member.getBusinessId())
                                .businessName(member.getBusinessName())
                                .role(member.getRole())
                                .branchIds(List.of())
                                .build())
                        .toList();

        builder.customerProfile(AuthCustomerProfileResponse.builder().isEnabled(true).build())
                .businessMemberships(businessMemberships)
                .platformMembership(buildPlatformMembership(user))
                .pendingInvitationsCount(Math.toIntExact(
                        businessInvitationService.countPendingByEmail(user.getEmail())));
    }

    private AuthPlatformMembershipResponse buildPlatformMembership(AppUserDto user) {
        PlatformMembershipDto membership = platformMembershipService.findActiveByUser(user.getId());
        if (membership == null) {
            return null;
        }
        return AuthPlatformMembershipResponse.builder()
                .role(membership.getRole().name())
                .permissions(membership.getPermissions().stream()
                        .map(Enum::name)
                        .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new)))
                .build();
    }
}
