package kz.ask.business.application;

import kz.ask.business.api.dto.CreateInviteRequest;
import kz.ask.business.api.dto.InviteResponse;
import kz.ask.business.domain.BranchInviteService;
import kz.ask.business.domain.BusinessBranchService;
import kz.ask.business.domain.BusinessService;
import kz.ask.business.domain.dto.BranchInviteDto;
import kz.ask.business.domain.enums.BranchMemberRole;
import kz.ask.identity.domain.IdentityService;
import kz.ask.identity.domain.dto.AppUserDto;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import kz.ask.shared.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InviteProcessor {

    private final IdentityService identityService;
    private final BusinessService businessService;
    private final BranchInviteService branchInviteService;
    private final BusinessBranchService businessBranchService;

    @Transactional
    public InviteResponse createInvite(AskPrincipal principal, UUID businessId, UUID branchId,
                                        CreateInviteRequest req) {
        verifyOwnerAccess(principal.getUserId(), businessId);
        requireBranchExists(businessId, branchId);

        AppUserDto createdBy = identityService.findById(principal.getUserId());
        Long ttlSeconds = 86400L;
        Integer maxUses = req.getMaxUses() != null && req.getMaxUses() > 0 ? req.getMaxUses() : 1;

        BranchInviteDto invite = branchInviteService.create(
                branchId, BranchMemberRole.STAFF, maxUses, ttlSeconds, createdBy.getId());
        return buildInviteResponse(invite);
    }

    public List<InviteResponse> listInvites(AskPrincipal principal, UUID businessId, UUID branchId) {
        verifyOwnerAccess(principal.getUserId(), businessId);
        requireBranchExists(businessId, branchId);
        List<BranchInviteDto> invites = branchInviteService.findByBranch(branchId);
        return invites.stream().map(this::buildInviteResponse).toList();
    }

    @Transactional
    public void revokeInvite(AskPrincipal principal, UUID businessId, UUID branchId, UUID inviteId) {
        verifyOwnerAccess(principal.getUserId(), businessId);
        requireBranchExists(businessId, branchId);
        branchInviteService.revoke(inviteId);
    }

    private void verifyOwnerAccess(UUID userId, UUID businessId) {
        if (!businessService.isOwnerOfBusiness(businessId, userId)) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
    }

    private void requireBranchExists(UUID businessId, UUID branchId) {
        if (businessBranchService.findByBusinessAndId(businessId, branchId) == null) {
            throw new NotFoundException(ErrorCode.BRANCH_NOT_FOUND);
        }
    }

    private InviteResponse buildInviteResponse(BranchInviteDto invite) {
        return InviteResponse.builder()
                .id(invite.getId())
                .code(invite.getCode())
                .role(invite.getRole())
                .maxUses(invite.getMaxUses())
                .useCount(invite.getUseCount())
                .expiresAt(invite.getExpiresAt())
                .revokedAt(invite.getRevokedAt())
                .build();
    }
}
