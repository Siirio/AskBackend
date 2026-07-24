package kz.ask.business.member.application;

import java.util.List;
import java.util.UUID;
import kz.ask.business.member.api.dto.BusinessMemberListResponse;
import kz.ask.business.member.api.dto.UpdateBusinessMemberRequest;
import kz.ask.business.member.domain.BusinessMemberService;
import kz.ask.business.member.domain.dto.BusinessMemberDto;
import kz.ask.identity.authorization.domain.enums.Role;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class BusinessMembersProcessor {

    private final BusinessMemberService businessMemberService;

    @Transactional(readOnly = true)
    public BusinessMemberListResponse list(AskPrincipal principal, UUID businessId) {
        if (!businessMemberService.isManagerOrAboveOfBusiness(businessId, principal.getUserId())) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
        List<BusinessMemberDto> members = businessMemberService.findByBusiness(businessId);
        return BusinessMemberListResponse.builder().members(members).build();
    }

    @Transactional
    public BusinessMemberDto updateRole(AskPrincipal principal, UUID membershipId,
                                        UpdateBusinessMemberRequest request) {
        BusinessMemberDto member = businessMemberService.findById(membershipId);
        if (!businessMemberService.isOwnerOfBusiness(member.getBusinessId(), principal.getUserId())) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
        return businessMemberService.updateRole(membershipId, request.getRole());
    }

    @Transactional
    public BusinessMemberDto deactivate(AskPrincipal principal, UUID membershipId) {
        BusinessMemberDto target = businessMemberService.findById(membershipId);
        Role actorRole = businessMemberService.getRoleInBusiness(target.getBusinessId(), principal.getUserId());
        boolean allowed = actorRole == Role.OWNER
                || (actorRole == Role.MANAGER
                && Role.WORKER.name().equals(target.getRole()));
        if (!allowed) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
        return businessMemberService.deactivate(membershipId);
    }
}
