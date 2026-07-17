package kz.ask.business.application;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import kz.ask.audit.domain.SignificantEventService;
import kz.ask.audit.domain.enums.SignificantEventType;
import kz.ask.business.api.dto.BusinessInvitationResponse;
import kz.ask.business.api.dto.CreateBusinessInvitationRequest;
import kz.ask.business.domain.BusinessBranchService;
import kz.ask.business.domain.BusinessInvitationService;
import kz.ask.business.domain.BusinessMemberBranchService;
import kz.ask.business.domain.BusinessMemberService;
import kz.ask.business.domain.dto.BusinessInvitationDto;
import kz.ask.business.domain.dto.BusinessMemberDto;
import kz.ask.business.domain.dto.CreatedBusinessInvitationDto;
import kz.ask.business.domain.enums.BusinessMemberRole;
import kz.ask.business.infrastructure.mail.BusinessInvitationEmailSender;
import kz.ask.identity.domain.IdentityService;
import kz.ask.identity.domain.dto.AppUserDto;
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
public class BusinessInvitationProcessor {

    private final BusinessInvitationService businessInvitationService;
    private final BusinessMemberService businessMemberService;
    private final BusinessMemberBranchService businessMemberBranchService;
    private final BusinessBranchService businessBranchService;
    private final IdentityService identityService;
    private final BusinessInvitationEmailSender businessInvitationEmailSender;
    private final SignificantEventService significantEventService;

    @Transactional
    public BusinessInvitationResponse create(
            AskPrincipal principal,
            UUID businessId,
            CreateBusinessInvitationRequest request) {
        BusinessMemberRole actorRole = requireManagementRole(businessId, principal.getUserId());
        BusinessMemberRole invitedRole = parseInvitedRole(request.getInvitedRole());
        if (actorRole == BusinessMemberRole.MANAGER && invitedRole != BusinessMemberRole.WORKER) {
            throw new ForbiddenException(ErrorCode.INVITATION_ROLE_NOT_ALLOWED);
        }
        validateBranches(businessId, request.getBranchIds());
        requireNotMember(businessId, request.getInvitedEmail());

        CreatedBusinessInvitationDto created = businessInvitationService.create(
                businessId,
                request.getInvitedEmail(),
                invitedRole,
                principal.getUserId(),
                request.getBranchIds());
        businessInvitationEmailSender.sendInvitation(
                request.getInvitedEmail(),
                created.getRawToken());
        significantEventService.record(principal.getUserId(),
                SignificantEventType.BUSINESS_INVITATION_SENT,
                businessId, created.getInvitation().getId(),
                Map.of("invitedRole", invitedRole.name()));
        return toResponse(created.getInvitation());
    }

    public List<BusinessInvitationResponse> listBusiness(
            AskPrincipal principal,
            UUID businessId) {
        requireManagementRole(businessId, principal.getUserId());
        return businessInvitationService.findByBusiness(businessId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void revoke(AskPrincipal principal, UUID businessId, UUID invitationId) {
        requireManagementRole(businessId, principal.getUserId());
        BusinessInvitationDto invitation = businessInvitationService.findById(invitationId);
        if (!invitation.getBusinessId().equals(businessId)) {
            throw new NotFoundException(ErrorCode.INVITATION_NOT_FOUND);
        }
        businessInvitationService.revoke(invitationId);
    }

    public List<BusinessInvitationResponse> listMine(AskPrincipal principal) {
        AppUserDto user = requireUser(principal.getUserId());
        return businessInvitationService.findPendingByEmail(user.getEmail()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public BusinessInvitationResponse accept(AskPrincipal principal, UUID invitationId) {
        AppUserDto user = requireUser(principal.getUserId());
        BusinessInvitationDto invitation = requireRecipient(invitationId, user.getEmail());
        if (businessMemberService.findByBusinessAndUser(
                invitation.getBusinessId(),
                user.getId()) != null) {
            throw new ConflictException(ErrorCode.INVITATION_MEMBER_EXISTS);
        }

        BusinessMemberDto membership = businessMemberService.createMember(
                invitation.getBusinessId(),
                user.getId(),
                BusinessMemberRole.valueOf(invitation.getInvitedRole()));
        invitation.getBranchIds().forEach(
                branchId -> businessMemberBranchService.assign(membership.getId(), branchId));
        significantEventService.record(user.getId(),
                SignificantEventType.BUSINESS_INVITATION_ACCEPTED,
                invitation.getBusinessId(), invitationId, Map.of());
        return toResponse(businessInvitationService.accept(invitationId, user.getId()));
    }

    @Transactional
    public BusinessInvitationResponse decline(AskPrincipal principal, UUID invitationId) {
        AppUserDto user = requireUser(principal.getUserId());
        BusinessInvitationDto invitation = requireRecipient(invitationId, user.getEmail());
        significantEventService.record(user.getId(),
                SignificantEventType.BUSINESS_INVITATION_DECLINED,
                invitation.getBusinessId(), invitationId, Map.of());
        return toResponse(businessInvitationService.decline(invitationId));
    }

    private BusinessMemberRole requireManagementRole(UUID businessId, UUID userId) {
        BusinessMemberRole role = businessMemberService.getRoleInBusiness(businessId, userId);
        if (role != BusinessMemberRole.OWNER && role != BusinessMemberRole.MANAGER) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
        return role;
    }

    private BusinessMemberRole parseInvitedRole(String role) {
        try {
            BusinessMemberRole parsed = BusinessMemberRole.valueOf(role.toUpperCase());
            if (parsed == BusinessMemberRole.OWNER) {
                throw new ValidationException(ErrorCode.INVITATION_ROLE_NOT_ALLOWED);
            }
            return parsed;
        } catch (IllegalArgumentException exception) {
            throw new ValidationException(ErrorCode.INVITATION_ROLE_NOT_ALLOWED);
        }
    }

    private void validateBranches(UUID businessId, Set<UUID> branchIds) {
        if (branchIds == null) {
            return;
        }
        branchIds.forEach(branchId -> {
            if (businessBranchService.findByBusinessAndId(businessId, branchId) == null) {
                throw new NotFoundException(ErrorCode.BRANCH_NOT_FOUND);
            }
        });
    }

    private void requireNotMember(UUID businessId, String email) {
        identityService.findAllActiveByEmail(email).stream()
                .filter(user -> businessMemberService.findByBusinessAndUser(
                        businessId,
                        user.getId()) != null)
                .findFirst()
                .ifPresent(user -> {
                    throw new ConflictException(ErrorCode.INVITATION_MEMBER_EXISTS);
                });
    }

    private BusinessInvitationDto requireRecipient(UUID invitationId, String email) {
        BusinessInvitationDto invitation = businessInvitationService.findById(invitationId);
        if (!invitation.getInvitedEmail().equalsIgnoreCase(email)) {
            throw new ForbiddenException(ErrorCode.INVITATION_EMAIL_MISMATCH);
        }
        return invitation;
    }

    private AppUserDto requireUser(UUID userId) {
        AppUserDto user = identityService.findById(userId);
        if (user == null) {
            throw new NotFoundException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    private BusinessInvitationResponse toResponse(BusinessInvitationDto invitation) {
        return BusinessInvitationResponse.builder()
                .id(invitation.getId())
                .businessId(invitation.getBusinessId())
                .businessName(invitation.getBusinessName())
                .invitedEmail(invitation.getInvitedEmail())
                .invitedRole(invitation.getInvitedRole())
                .invitedByDisplayName(invitation.getInvitedByDisplayName())
                .status(invitation.getStatus())
                .expiresAt(invitation.getExpiresAt())
                .branchIds(invitation.getBranchIds())
                .build();
    }
}
