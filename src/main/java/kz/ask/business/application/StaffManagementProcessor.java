package kz.ask.business.application;

import java.util.List;
import java.util.UUID;
import kz.ask.business.api.dto.CreateStaffRequest;
import kz.ask.business.api.dto.StaffResponse;
import kz.ask.business.api.dto.UpdateStaffRequest;
import kz.ask.business.domain.BranchMemberService;
import kz.ask.business.domain.BusinessBranchService;
import kz.ask.business.domain.BusinessMemberService;
import kz.ask.business.domain.BusinessService;
import kz.ask.business.domain.dto.BranchMemberDto;
import kz.ask.business.domain.dto.BusinessMemberDto;
import kz.ask.business.domain.enums.BranchMemberRole;
import kz.ask.business.domain.enums.BusinessMemberRole;
import kz.ask.identity.domain.IdentityService;
import kz.ask.identity.domain.dto.AppUserDto;
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
    private final BusinessMemberService businessMemberService;
    private final BranchMemberService branchMemberService;
    private final BusinessBranchService businessBranchService;

    @Transactional
    public StaffResponse createStaff(AskPrincipal principal, UUID businessId, UUID branchId,
                                      CreateStaffRequest req) {
        verifyManagementAccess(principal.getUserId(), businessId);
        requireBranchExists(businessId, branchId);

        BranchMemberRole memberRole = resolveBranchMemberRole(req.getRole());
        requireCanAssignRole(principal.getUserId(), businessId, memberRole);

        if (identityService.findByEmail(req.getEmail()) != null) {
            throw new ConflictException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String tempPassword = generateTempPassword();
        AppUserDto user = identityService.createStaffUser(req.getEmail(), req.getDisplayName(), tempPassword);

        BusinessMemberRole bizRole = mapToBusinessRole(memberRole);
        businessMemberService.createMember(businessId, user.getId(), bizRole);

        BranchMemberDto member = branchMemberService.addMember(branchId, user.getId(), memberRole);

        return buildStaffResponse(member, tempPassword);
    }

    public List<StaffResponse> listStaff(AskPrincipal principal, UUID businessId, UUID branchId) {
        verifyManagementAccess(principal.getUserId(), businessId);
        requireBranchExists(businessId, branchId);
        List<BranchMemberDto> members = branchMemberService.findByBranch(branchId);
        return members.stream().map(m -> buildStaffResponse(m, null)).toList();
    }

    @Transactional
    public StaffResponse updateStaff(AskPrincipal principal, UUID businessId, UUID branchId,
                                      UUID staffId, UpdateStaffRequest req) {
        verifyManagementAccess(principal.getUserId(), businessId);
        requireBranchExists(businessId, branchId);
        BranchMemberDto member = requireBranchMember(staffId, branchId);

        if (req.getStatus() != null) {
            identityService.updateUserStatus(member.getUserId(), req.getStatus());
        }

        if (req.getRole() != null) {
            BranchMemberRole newRole = resolveBranchMemberRole(req.getRole());
            requireCanAssignRole(principal.getUserId(), businessId, newRole);
            branchMemberService.updateMemberRole(staffId, newRole);
        }

        return buildStaffResponse(member, null);
    }

    @Transactional
    public StaffResponse resetPassword(AskPrincipal principal, UUID businessId, UUID branchId, UUID staffId) {
        verifyManagementAccess(principal.getUserId(), businessId);
        requireBranchExists(businessId, branchId);
        BranchMemberDto member = requireBranchMember(staffId, branchId);

        String newTempPassword = generateTempPassword();
        identityService.resetStaffPassword(member.getUserId(), newTempPassword);

        return buildStaffResponse(member, newTempPassword);
    }

    private void verifyManagementAccess(UUID userId, UUID businessId) {
        if (!businessService.isManagerOrAboveOfBusiness(businessId, userId)) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
    }

    private void requireCanAssignRole(UUID userId, UUID businessId, BranchMemberRole targetRole) {
        BusinessMemberRole actorRole = businessMemberService.getRoleInBusiness(businessId, userId);
        if (actorRole == null) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
        if (actorRole == BusinessMemberRole.MANAGER && targetRole != BranchMemberRole.WORKER) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
    }

    private BranchMemberRole resolveBranchMemberRole(String role) {
        if (role == null || role.isBlank()) {
            return BranchMemberRole.WORKER;
        }
        try {
            return BranchMemberRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return BranchMemberRole.WORKER;
        }
    }

    private BusinessMemberRole mapToBusinessRole(BranchMemberRole branchRole) {
        return switch (branchRole) {
            case MANAGER -> BusinessMemberRole.MANAGER;
            default -> BusinessMemberRole.WORKER;
        };
    }

    private void requireBranchExists(UUID businessId, UUID branchId) {
        if (businessBranchService.findByBusinessAndId(businessId, branchId) == null) {
            throw new NotFoundException(ErrorCode.BRANCH_NOT_FOUND);
        }
    }

    private BranchMemberDto requireBranchMember(UUID staffId, UUID branchId) {
        return branchMemberService.findByBranch(branchId).stream()
                .filter(m -> m.getId().equals(staffId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(ErrorCode.STAFF_NOT_FOUND));
    }

    private StaffResponse buildStaffResponse(BranchMemberDto member, String tempPassword) {
        return StaffResponse.builder()
                .id(member.getId())
                .email(member.getUserEmail())
                .displayName(member.getUserDisplayName())
                .role(member.getRole())
                .status(member.getUserStatus())
                .tempPassword(tempPassword)
                .activatedAt(member.getUserActivatedAt())
                .build();
    }

    private String generateTempPassword() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
