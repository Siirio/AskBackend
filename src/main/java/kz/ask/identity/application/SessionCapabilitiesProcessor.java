package kz.ask.identity.application;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
        AuthPlatformMembershipResponse platformMembership = buildPlatformMembership(user);

        builder.customerProfile(AuthCustomerProfileResponse.builder().isEnabled(true).build())
                .businessMemberships(businessMemberships)
                .platformMembership(platformMembership)
                .allRoles(buildAllRoles(
                        user,
                        businessMemberships.stream()
                                .map(AuthBusinessMembershipResponse::getRole)
                                .toList(),
                        platformMembership))
                .pendingInvitationsCount(Math.toIntExact(
                        businessInvitationService.countPendingByEmail(user.getEmail())));
    }

    public List<String> resolveAllRoles(AppUserDto user) {
        List<String> businessRoles =
                businessMemberService.findActiveByUser(user.getId()).stream()
                        .map(member -> member.getRole())
                        .toList();
        return buildAllRoles(user, businessRoles, buildPlatformMembership(user));
    }

    private List<String> buildAllRoles(AppUserDto user,
                                       List<String> businessRoles,
                                       AuthPlatformMembershipResponse platformMembership) {
        Set<String> roles = new LinkedHashSet<>();
        roles.add(user.getRole().name());
        roles.addAll(businessRoles);
        if (platformMembership != null) {
            roles.add(platformMembership.getRole());
        }
        return List.copyOf(roles);
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
