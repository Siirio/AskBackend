package kz.ask.business.member.application;

import java.util.List;
import java.util.UUID;
import kz.ask.business.member.api.dto.CreateEmployeeRequest;
import kz.ask.business.member.api.dto.CreateStaffRequest;
import kz.ask.business.member.api.dto.StaffResponse;
import kz.ask.business.member.api.dto.UpdateStaffRequest;
import kz.ask.business.member.domain.BranchMemberService;
import kz.ask.business.branch.domain.BusinessBranchService;
import kz.ask.business.member.domain.BusinessMemberService;
import kz.ask.business.core.domain.BusinessService;
import kz.ask.business.member.domain.dto.BranchMemberDto;
import kz.ask.business.member.domain.dto.BusinessMemberDto;
import kz.ask.identity.domain.IdentityService;
import kz.ask.identity.domain.dto.AppUserDto;
import kz.ask.identity.domain.enums.UserStatus;
import kz.ask.identity.authorization.domain.enums.Role;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.shared.error.ConflictException;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import kz.ask.shared.error.NotFoundException;
import kz.ask.shared.error.ValidationException;
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

        Role memberRole = resolveBranchMemberRole(req.getRole());
        requireCanAssignRole(principal.getUserId(), businessId, memberRole);

        Role appRole = memberRole == Role.MANAGER ? Role.MANAGER : Role.WORKER;

        if (!identityService.findAllByEmail(req.getEmail()).isEmpty()) {
            throw new ConflictException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String tempPassword = generateTempPassword();
        AppUserDto user = identityService.createStaffUser(req.getEmail(), req.getDisplayName(), tempPassword, appRole);

        Role bizRole = mapToBusinessRole(memberRole);
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
            Role newRole = resolveBranchMemberRole(req.getRole());
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

    private void requireCanAssignRole(UUID userId, UUID businessId, Role targetRole) {
        Role actorRole = businessMemberService.getRoleInBusiness(businessId, userId);
        if (actorRole == null) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
        if (actorRole == Role.MANAGER && targetRole != Role.WORKER) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
    }

    private Role resolveBranchMemberRole(String role) {
        if (role == null || role.isBlank()) {
            return Role.WORKER;
        }
        try {
            Role parsed = Role.valueOf(role.toUpperCase());
            return parsed == Role.MANAGER || parsed == Role.WORKER ? parsed : Role.WORKER;
        } catch (IllegalArgumentException e) {
            return Role.WORKER;
        }
    }

    private Role mapToBusinessRole(Role branchRole) {
        return switch (branchRole) {
            case MANAGER -> Role.MANAGER;
            default -> Role.WORKER;
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
                .branchName(member.getBranchName())
                .status(member.getUserStatus())
                .tempPassword(tempPassword != null
                        ? tempPassword
                        : identityService.revealTemporaryPassword(member.getUserId()))
                .activatedAt(member.getUserActivatedAt())
                .build();
    }

    private StaffResponse buildStaffResponse(BranchMemberDto member, String tempPassword,
                                             String businessName, UUID businessId) {
        return StaffResponse.builder()
                .id(member.getId())
                .email(member.getUserEmail())
                .displayName(member.getUserDisplayName())
                .role(member.getRole())
                .branchName(member.getBranchName())
                .status(member.getUserStatus())
                .tempPassword(tempPassword != null
                        ? tempPassword
                        : identityService.revealTemporaryPassword(member.getUserId()))
                .activatedAt(member.getUserActivatedAt())
                .businessName(businessName)
                .businessId(businessId)
                .build();
    }

    private String generateTempPassword() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    @Transactional
    public StaffResponse createEmployee(AskPrincipal principal, UUID businessId,
                                        CreateEmployeeRequest req) {
        verifyManagementAccess(principal.getUserId(), businessId);

        Role branchRole = resolveBranchMemberRole(req.getRole());
        requireCanAssignRole(principal.getUserId(), businessId, branchRole);

        if (branchRole == Role.WORKER && req.getBranchId() == null) {
            throw new ValidationException(ErrorCode.BRANCH_REQUIRED_FOR_WORKER);
        }
        if (req.getBranchId() != null) {
            requireBranchExists(businessId, req.getBranchId());
        }

        Role appRole = branchRole == Role.MANAGER ? Role.MANAGER : Role.WORKER;

        if (!identityService.findAllByEmail(req.getEmail()).isEmpty()) {
            throw new ConflictException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String tempPassword = generateTempPassword();
        AppUserDto user = identityService.createStaffUser(req.getEmail(), req.getDisplayName(), tempPassword, appRole);

        Role bizRole = mapToBusinessRole(branchRole);
        BusinessMemberDto bizMember = businessMemberService.createMember(businessId, user.getId(), bizRole);

        BranchMemberDto branchMember = null;
        if (req.getBranchId() != null) {
            branchMember = branchMemberService.addMember(req.getBranchId(), user.getId(), branchRole);
        }

        String businessName = businessService.findByMember(user.getId()).getBusiness().getName();

        if (branchMember != null) {
            StaffResponse response = buildStaffResponse(branchMember, tempPassword, businessName, businessId);
            response.setId(bizMember.getId());
            return response;
        }
        return buildEmployeeResponse(bizMember, user, tempPassword, businessName, businessId);
    }

    public List<StaffResponse> listEmployees(AskPrincipal principal, UUID businessId) {
        verifyManagementAccess(principal.getUserId(), businessId);
        List<BusinessMemberDto> members = businessMemberService.findByBusiness(businessId);
        return members.stream()
                .map(m -> buildEmployeeResponseFromDto(m, businessId))
                .toList();
    }

    @Transactional
    public void deletePendingEmployee(AskPrincipal principal, UUID businessId, UUID membershipId) {
        verifyManagementAccess(principal.getUserId(), businessId);
        BusinessMemberDto member = businessMemberService.findById(membershipId);
        if (!businessId.equals(member.getBusinessId())) {
            throw new NotFoundException(ErrorCode.STAFF_NOT_FOUND);
        }
        requireCanAssignRole(principal.getUserId(), businessId, Role.valueOf(member.getRole()));
        AppUserDto user = identityService.findById(member.getUserId());
        if (user == null || user.getActivatedAt() != null
                || user.getStatus() != UserStatus.PENDING_ACTIVATION) {
            throw new ValidationException(ErrorCode.STAFF_ALREADY_ACTIVATED);
        }
        branchMemberService.removeByUser(member.getUserId());
        businessMemberService.deactivate(member.getId());
        identityService.deletePendingStaffUser(member.getUserId());
    }

    private StaffResponse buildEmployeeResponse(BusinessMemberDto member, AppUserDto user,
                                                String tempPassword, String businessName, UUID businessId) {
        return StaffResponse.builder()
                .id(member.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .role(member.getRole())
                .branchName(null)
                .status(user.getStatus().name())
                .tempPassword(tempPassword)
                .activatedAt(user.getActivatedAt())
                .businessName(businessName)
                .businessId(businessId)
                .build();
    }

    private StaffResponse buildEmployeeResponseFromDto(BusinessMemberDto member, UUID businessId) {
        AppUserDto user = identityService.findById(member.getUserId());
        BranchMemberDto branchMember = branchMemberService.findByUser(member.getUserId());
        return StaffResponse.builder()
                .id(member.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .role(member.getRole())
                .branchName(branchMember != null ? branchMember.getBranchName() : null)
                .status(user.getStatus().name())
                .tempPassword(identityService.revealTemporaryPassword(member.getUserId()))
                .activatedAt(user.getActivatedAt())
                .businessId(businessId)
                .build();
    }
}
