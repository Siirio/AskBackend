package kz.ask.business.member.application;

import java.util.List;
import java.util.UUID;
import kz.ask.business.member.api.dto.UpdateBusinessMemberRequest;
import kz.ask.business.member.domain.BusinessMemberService;
import kz.ask.business.member.domain.dto.BusinessMemberDto;
import kz.ask.identity.authorization.domain.enums.Role;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import kz.ask.shared.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class BusinessMembersProcessor {

    private final BusinessMemberService businessMemberService;

    @Transactional(readOnly = true)
    public List<BusinessMemberDto> list(AskPrincipal principal, UUID businessId) {
        if (!businessMemberService.isManagerOrAboveOfBusiness(businessId, principal.getUserId())) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
        return businessMemberService.findByBusiness(businessId);
    }

    @Transactional
    public BusinessMemberDto updateRole(AskPrincipal principal, UUID businessId, UUID membershipId,
                                        UpdateBusinessMemberRequest request) {
        if (!businessMemberService.isOwnerOfBusiness(businessId, principal.getUserId())) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
        requireMemberOfBusiness(businessId, membershipId);
        return businessMemberService.updateRole(membershipId, request.getRole());
    }

    @Transactional
    public BusinessMemberDto deactivate(AskPrincipal principal, UUID businessId, UUID membershipId) {
        BusinessMemberDto target = requireMemberOfBusiness(businessId, membershipId);
        Role actorRole = businessMemberService.getRoleInBusiness(businessId, principal.getUserId());
        boolean allowed = actorRole == Role.OWNER
                || (actorRole == Role.MANAGER
                && Role.WORKER.name().equals(target.getRole()));
        if (!allowed) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
        return businessMemberService.deactivate(membershipId);
    }

    private BusinessMemberDto requireMemberOfBusiness(UUID businessId, UUID membershipId) {
        BusinessMemberDto member = businessMemberService.findById(membershipId);
        if (!businessId.equals(member.getBusinessId())) {
            throw new NotFoundException(ErrorCode.BUSINESS_MEMBER_NOT_FOUND);
        }
        return member;
    }
}
