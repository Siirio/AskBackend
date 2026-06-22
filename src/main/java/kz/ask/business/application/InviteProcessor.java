package kz.ask.business.application;

import java.util.List;
import java.util.UUID;
import kz.ask.business.api.dto.CreateInviteRequest;
import kz.ask.business.api.dto.InviteResponse;
import kz.ask.business.domain.BusinessService;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.business.domain.enums.BranchMemberRole;
import kz.ask.business.infrastructure.mapper.BusinessMapper;
import kz.ask.identity.domain.IdentityService;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import kz.ask.shared.error.NotFoundException;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class InviteProcessor {

    private final IdentityService identityService;
    private final BusinessService businessService;
    private final BusinessMapper businessMapper;

    @Transactional
    public InviteResponse createInvite(AskPrincipal principal, UUID businessId, UUID branchId,
                                        CreateInviteRequest req) {
        verifyOwnerAccess(principal.getUserId(), businessId);
        BusinessBranch branch = requireBranch(businessId, branchId);

        AppUser createdBy = identityService.findById(principal.getUserId());
        Long ttlSeconds = 86400L;
        Integer maxUses = req.getMaxUses() != null && req.getMaxUses() > 0 ? req.getMaxUses() : 1;

        return businessMapper.toInviteResponse(
                businessService.createInvite(branch, BranchMemberRole.STAFF, maxUses, ttlSeconds, createdBy));
    }

    public List<InviteResponse> listInvites(AskPrincipal principal, UUID businessId, UUID branchId) {
        verifyOwnerAccess(principal.getUserId(), businessId);
        requireBranchExists(businessId, branchId);
        return businessMapper.toInviteResponseList(businessService.findBranchInvites(branchId));
    }

    @Transactional
    public void revokeInvite(AskPrincipal principal, UUID businessId, UUID branchId, UUID inviteId) {
        verifyOwnerAccess(principal.getUserId(), businessId);
        requireBranchExists(businessId, branchId);
        businessService.revokeInvite(inviteId);
    }

    private void verifyOwnerAccess(UUID userId, UUID businessId) {
        if (!businessService.isOwnerOfBusiness(businessId, userId)) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
    }

    private BusinessBranch requireBranch(UUID businessId, UUID branchId) {
        BusinessBranch branch = businessService.findBranchByBusinessAndId(businessId, branchId);
        if (branch == null) {
            throw new NotFoundException(ErrorCode.BRANCH_NOT_FOUND);
        }
        return branch;
    }

    private void requireBranchExists(UUID businessId, UUID branchId) {
        if (businessService.findBranchByBusinessAndId(businessId, branchId) == null) {
            throw new NotFoundException(ErrorCode.BRANCH_NOT_FOUND);
        }
    }
}
