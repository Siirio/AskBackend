package kz.ask.identity.application;

import kz.ask.business.domain.BusinessDomainService.BusinessRegistrationResult;
import kz.ask.identity.api.dto.AuthBusinessContextResponse;
import kz.ask.identity.api.dto.AuthChallengeResponse;
import kz.ask.identity.api.dto.AuthSessionResponse;
import kz.ask.identity.api.dto.AuthUserResponse;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.identity.domain.entity.AuthChallenge;
import kz.ask.identity.domain.entity.AuthSession;

final class AuthAssembler {

    private AuthAssembler() {}

    static AuthChallengeResponse toChallengeResponse(AuthChallenge challenge, String maskedDestination, String role) {
        AuthChallengeResponse r = new AuthChallengeResponse();
        r.setAuthChallengeId(challenge.getId());
        r.setRole(role);
        r.setPurpose(challenge.getPurpose().name());
        r.setChannel(challenge.getChannel().name());
        r.setMaskedDestination(maskedDestination);
        r.setExpiresAt(challenge.getExpiresAt());
        return r;
    }

    static AuthSessionResponse toSessionResponse(AuthSession session,
                                                  AppUser user,
                                                  BusinessRegistrationResult bizResult) {
        AuthSessionResponse r = new AuthSessionResponse();
        if (session != null) {
            r.setAccessToken(session.getPlainToken());
            r.setExpiresAt(session.getExpiresAt());
            r.setRemembered(session.isRemembered());
            r.setRole(session.getRole().name());
            r.setStartRoute("BUSINESS".equals(session.getRole().name()) ? "BUSINESS_ACTIVITY" : "CLIENT_SEARCH");
        } else {
            r.setRole(user.getRole().name());
            r.setStartRoute("BUSINESS".equals(user.getRole().name()) ? "BUSINESS_ACTIVITY" : "CLIENT_SEARCH");
        }
        r.setTokenType("Bearer");
        r.setUser(toUserResponse(user));
        if (bizResult != null) {
            r.setBusiness(toBusinessContextResponse(bizResult));
        }
        return r;
    }

    static AuthUserResponse toUserResponse(AppUser user) {
        AuthUserResponse r = new AuthUserResponse();
        r.setUserId(user.getId());
        r.setDisplayName(user.getDisplayName());
        r.setEmail(user.getEmail());
        r.setPhone(user.getPhone());
        r.setStatus(user.getStatus().name());
        return r;
    }

    static AuthBusinessContextResponse toBusinessContextResponse(BusinessRegistrationResult bizResult) {
        AuthBusinessContextResponse r = new AuthBusinessContextResponse();
        r.setBusinessId(bizResult.business.getId());
        r.setBusinessName(bizResult.business.getName());
        r.setBranchId(bizResult.branch.getId());
        r.setBranchName(bizResult.branch.getName());
        r.setMembershipId(bizResult.member.getId());
        r.setMemberRole(bizResult.member.getRole().name());
        return r;
    }
}
