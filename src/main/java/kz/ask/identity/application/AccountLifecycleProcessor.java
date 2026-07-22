package kz.ask.identity.application;

import java.util.List;
import java.util.Map;
import kz.ask.audit.domain.SignificantEventService;
import kz.ask.audit.domain.enums.SignificantEventType;
import kz.ask.business.domain.entity.BusinessMember;
import kz.ask.business.domain.enums.BusinessInvitationStatus;
import kz.ask.business.domain.enums.BusinessMemberRole;
import kz.ask.business.infrastructure.repository.BusinessInvitationRepository;

import kz.ask.business.infrastructure.repository.BusinessMemberRepository;
import kz.ask.chat.domain.ChatService;
import kz.ask.identity.api.dto.LogoutResponse;
import kz.ask.identity.domain.IdentityService;
import kz.ask.identity.domain.dto.AppUserDto;
import kz.ask.identity.domain.entity.CustomerProfile;
import kz.ask.identity.infrastructure.repository.CustomerProfileRepository;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.platform.infrastructure.repository.PlatformMembershipRepository;

import kz.ask.shared.error.ConflictException;
import kz.ask.shared.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AccountLifecycleProcessor {

    private final IdentityService identityService;
    private final BusinessMemberRepository businessMemberRepository;

    private final BusinessInvitationRepository businessInvitationRepository;
    private final PlatformMembershipRepository platformMembershipRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final SignificantEventService significantEventService;
    private final ChatService chatService;

    @Transactional
    public LogoutResponse delete(AskPrincipal principal) {
        AppUserDto user = identityService.findById(principal.getUserId());
        significantEventService.record(
                user.getId(), SignificantEventType.ACCOUNT_DELETION_REQUESTED, null, user.getId(), Map.of());
        List<BusinessMember> memberships =
                businessMemberRepository.findByUserId(user.getId());
        for (BusinessMember membership : memberships) {
            if (membership.getRole() == BusinessMemberRole.OWNER && isSoleOwner(membership)) {
                throw new ConflictException(ErrorCode.ACCOUNT_OWNER_TRANSFER_REQUIRED);
            }
        }
        businessInvitationRepository.deleteAll(
                businessInvitationRepository.findByInvitedEmailIgnoreCaseAndStatus(
                        user.getEmail(), BusinessInvitationStatus.PENDING));

        platformMembershipRepository.findByUserId(user.getId())
                .ifPresent(platformMembershipRepository::delete);
        customerProfileRepository.findByUserId(user.getId())
                .ifPresent(this::anonymizeProfile);
        chatService.deleteCustomerConversations(user.getId());
        identityService.anonymizeAccount(user.getId());
        significantEventService.record(
                user.getId(), SignificantEventType.ACCOUNT_DELETED, null, user.getId(), Map.of());
        return LogoutResponse.builder().success(true).build();
    }

    private boolean isSoleOwner(BusinessMember membership) {
        return businessMemberRepository
                .findByBusinessId(membership.getBusiness().getId())
                .stream()
                .filter(member -> member.getRole() == BusinessMemberRole.OWNER)
                .count() == 1;
    }

    private void anonymizeProfile(CustomerProfile profile) {
        profile.setDisplayName(null);
        profile.setIconUrl(null);
    }

}
