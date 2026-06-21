package kz.ask.business.application;

import java.util.List;
import java.util.UUID;
import kz.ask.business.api.dto.CreateStaffRequest;
import kz.ask.business.api.dto.StaffResponse;
import kz.ask.business.api.dto.UpdateStaffRequest;
import kz.ask.business.domain.BusinessService;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.business.domain.entity.BranchMember;
import kz.ask.business.infrastructure.mapper.BusinessMapper;
import kz.ask.identity.domain.IdentityService;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.identity.domain.enums.UserStatus;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.shared.error.ConflictException;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import kz.ask.shared.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class StaffManagementProcessor {

    private final IdentityService identityService;
    private final BusinessService businessService;
    private final BusinessMapper businessMapper;

    @Transactional
    public StaffResponse createStaff(AskPrincipal principal, UUID businessId, UUID branchId,
                                      CreateStaffRequest req) {
        verifyBranchAccess(principal.getUserId(), branchId);
        BusinessBranch branch = requireBranch(businessId, branchId);

        if (identityService.findByEmail(req.getEmail()) != null) {
            throw new ConflictException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String tempPassword = generateTempPassword();
        AppUser user = identityService.createStaffUser(req.getEmail(), req.getDisplayName(), tempPassword);
        BranchMember member = businessService.addBranchMember(branch, user, req.getRole());

        return businessMapper.toStaffResponse(member, tempPassword);
    }

    public List<StaffResponse> listStaff(AskPrincipal principal, UUID businessId, UUID branchId) {
        verifyBranchAccess(principal.getUserId(), branchId);
        requireBranchExists(businessId, branchId);
        return businessMapper.toStaffResponseList(businessService.findBranchMembers(branchId));
    }

    @Transactional
    public StaffResponse updateStaff(AskPrincipal principal, UUID businessId, UUID branchId,
                                      UUID staffId, UpdateStaffRequest req) {
        verifyBranchAccess(principal.getUserId(), branchId);
        requireBranchExists(businessId, branchId);
        BranchMember member = requireBranchMember(staffId, branchId);

        if (req.getRole() != null) {
            member.setRole(req.getRole());
        }
        if (req.getStatus() != null) {
            AppUser user = member.getUser();
            user.setStatus(UserStatus.valueOf(req.getStatus()));
        }

        return businessMapper.toStaffResponse(member, null);
    }

    @Transactional
    public StaffResponse resetPassword(AskPrincipal principal, UUID businessId, UUID branchId, UUID staffId) {
        verifyBranchAccess(principal.getUserId(), branchId);
        requireBranchExists(businessId, branchId);
        BranchMember member = requireBranchMember(staffId, branchId);

        String newTempPassword = generateTempPassword();
        identityService.resetStaffPassword(member.getUser(), newTempPassword);

        return businessMapper.toStaffResponse(member, newTempPassword);
    }

    private void verifyBranchAccess(UUID userId, UUID branchId) {
        if (!businessService.isOwnerOrManagerOfBranch(branchId, userId)) {
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

    private BranchMember requireBranchMember(UUID staffId, UUID branchId) {
        return businessService.findBranchMembers(branchId).stream()
                .filter(m -> m.getId().equals(staffId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(ErrorCode.STAFF_NOT_FOUND));
    }

    private String generateTempPassword() {
        return java.util.UUID.randomUUID().toString().substring(0, 8);
    }
}
