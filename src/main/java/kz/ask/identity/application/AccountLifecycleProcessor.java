package kz.ask.identity.application;

import java.util.List;
import java.util.Map;
import kz.ask.audit.domain.SignificantEventService;
import kz.ask.audit.domain.enums.SignificantEventType;
import kz.ask.business.domain.entity.BusinessMember;
import kz.ask.business.domain.enums.BusinessInvitationStatus;
import kz.ask.business.domain.enums.BusinessMemberRole;
import kz.ask.business.infrastructure.repository.BusinessInvitationRepository;
import kz.ask.business.infrastructure.repository.BusinessMemberBranchRepository;
import kz.ask.business.infrastructure.repository.BusinessMemberRepository;
import kz.ask.identity.api.dto.AccountExportResponse;
import kz.ask.identity.api.dto.LogoutResponse;
import kz.ask.identity.domain.IdentityService;
import kz.ask.identity.domain.dto.AppUserDto;
import kz.ask.identity.domain.entity.CustomerProfile;
import kz.ask.identity.infrastructure.repository.CustomerProfileRepository;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.legal.domain.entity.LegalAcceptance;
import kz.ask.legal.infrastructure.repository.LegalAcceptanceRepository;
import kz.ask.platform.domain.entity.PlatformMembership;
import kz.ask.platform.infrastructure.repository.PlatformMembershipRepository;
import kz.ask.shared.domain.enums.RecordStatus;
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
    private final BusinessMemberBranchRepository businessMemberBranchRepository;
    private final BusinessInvitationRepository businessInvitationRepository;
    private final PlatformMembershipRepository platformMembershipRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final LegalAcceptanceRepository legalAcceptanceRepository;
    private final SignificantEventService significantEventService;

    @Transactional(readOnly = true)
    public AccountExportResponse export(AskPrincipal principal) {
        AppUserDto user = identityService.findById(principal.getUserId());
        List<AccountExportResponse.BusinessMembershipExport> memberships =
                businessMemberRepository.findByUserIdAndStatus(user.getId(), RecordStatus.ACTIVE)
                        .stream()
                        .map(this::toBusinessMembership)
                        .toList();
        AccountExportResponse.PlatformMembershipExport platform =
                platformMembershipRepository.findByUserIdAndStatus(user.getId(), RecordStatus.ACTIVE)
                        .map(this::toPlatformMembership)
                        .orElse(null);
        List<AccountExportResponse.LegalAcceptanceExport> acceptances =
                legalAcceptanceRepository.findByUserIdOrderByAcceptedAtAsc(user.getId())
                        .stream()
                        .map(this::toLegalAcceptance)
                        .toList();
        return AccountExportResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .status(user.getStatus().name())
                .businessMemberships(memberships)
                .platformMembership(platform)
                .legalAcceptances(acceptances)
                .build();
    }

    @Transactional
    public LogoutResponse delete(AskPrincipal principal) {
        AppUserDto user = identityService.findById(principal.getUserId());
        significantEventService.record(
                user.getId(), SignificantEventType.ACCOUNT_DELETION_REQUESTED, null, user.getId(), Map.of());
        List<BusinessMember> memberships =
                businessMemberRepository.findByUserIdAndStatus(user.getId(), RecordStatus.ACTIVE);
        for (BusinessMember membership : memberships) {
            if (membership.getRole() == BusinessMemberRole.OWNER && isSoleOwner(membership)) {
                throw new ConflictException(ErrorCode.ACCOUNT_OWNER_TRANSFER_REQUIRED);
            }
        }
        businessInvitationRepository.deleteAll(
                businessInvitationRepository.findByInvitedEmailIgnoreCaseAndStatus(
                        user.getEmail(), BusinessInvitationStatus.PENDING));
        for (BusinessMember membership : memberships) {
            businessMemberBranchRepository.deleteByBusinessMembershipId(membership.getId());
            membership.setStatus(RecordStatus.INACTIVE);
        }
        platformMembershipRepository.findByUserIdAndStatus(user.getId(), RecordStatus.ACTIVE)
                .ifPresent(membership -> membership.setStatus(RecordStatus.INACTIVE));
        customerProfileRepository.findByUserId(user.getId())
                .ifPresent(this::anonymizeProfile);
        identityService.anonymizeAccount(user.getId());
        significantEventService.record(
                user.getId(), SignificantEventType.ACCOUNT_DELETED, null, user.getId(), Map.of());
        return LogoutResponse.builder().success(true).build();
    }

    private boolean isSoleOwner(BusinessMember membership) {
        return businessMemberRepository
                .findByBusinessIdAndStatus(membership.getBusiness().getId(), RecordStatus.ACTIVE)
                .stream()
                .filter(member -> member.getRole() == BusinessMemberRole.OWNER)
                .count() == 1;
    }

    private void anonymizeProfile(CustomerProfile profile) {
        profile.setDisplayName(null);
        profile.setIconUrl(null);
    }

    private AccountExportResponse.BusinessMembershipExport toBusinessMembership(BusinessMember membership) {
        return AccountExportResponse.BusinessMembershipExport.builder()
                .businessId(membership.getBusiness().getId())
                .businessName(membership.getBusiness().getName())
                .role(membership.getRole().name())
                .status(membership.getStatus().name())
                .build();
    }

    private AccountExportResponse.PlatformMembershipExport toPlatformMembership(
            PlatformMembership membership) {
        return AccountExportResponse.PlatformMembershipExport.builder()
                .role(membership.getRole().name())
                .status(membership.getStatus().name())
                .permissions(membership.getPermissions().stream().map(Enum::name).sorted().toList())
                .build();
    }

    private AccountExportResponse.LegalAcceptanceExport toLegalAcceptance(
            LegalAcceptance acceptance) {
        return AccountExportResponse.LegalAcceptanceExport.builder()
                .documentCode(acceptance.getDocumentCode().name())
                .documentVersion(acceptance.getDocumentVersion())
                .countryCode(acceptance.getCountryCode())
                .locale(acceptance.getLocale())
                .acceptanceChannel(acceptance.getAcceptanceChannel().name())
                .acceptedAt(acceptance.getAcceptedAt())
                .build();
    }
}
