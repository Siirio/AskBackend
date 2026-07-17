package kz.ask.business.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import kz.ask.business.domain.dto.BusinessInvitationDto;
import kz.ask.business.domain.dto.CreatedBusinessInvitationDto;
import kz.ask.business.domain.entity.BusinessInvitation;
import kz.ask.business.domain.enums.BusinessInvitationStatus;
import kz.ask.business.domain.enums.BusinessMemberRole;
import kz.ask.business.infrastructure.mapper.BusinessInvitationMapper;
import kz.ask.business.infrastructure.repository.BusinessInvitationRepository;
import kz.ask.business.infrastructure.repository.BusinessRepository;
import kz.ask.identity.infrastructure.repository.AppUserRepository;
import kz.ask.shared.error.ConflictException;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.InternalServerException;
import kz.ask.shared.error.NotFoundException;
import kz.ask.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BusinessInvitationServiceImpl implements BusinessInvitationService {

    private final BusinessInvitationRepository businessInvitationRepository;
    private final BusinessRepository businessRepository;
    private final AppUserRepository appUserRepository;
    private final BusinessInvitationMapper businessInvitationMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${business.invitation.ttl-seconds:604800}")
    private Long invitationTtlSeconds;

    @Value("${business.invitation.token-bytes:32}")
    private Integer tokenBytes;

    @Override
    @Transactional
    public CreatedBusinessInvitationDto create(
            UUID businessId,
            String invitedEmail,
            BusinessMemberRole invitedRole,
            UUID invitedByUserId,
            Set<UUID> branchIds) {
        String normalizedEmail = invitedEmail.trim().toLowerCase();
        if (businessInvitationRepository
                .existsByBusinessIdAndInvitedEmailIgnoreCaseAndInvitedRoleAndStatus(
                        businessId,
                        normalizedEmail,
                        invitedRole,
                        BusinessInvitationStatus.PENDING)) {
            throw new ConflictException(ErrorCode.INVITATION_ALREADY_PENDING);
        }

        String rawToken = createRawToken();
        BusinessInvitation invitation = new BusinessInvitation();
        invitation.setBusiness(businessRepository.getReferenceById(businessId));
        invitation.setInvitedEmail(normalizedEmail);
        invitation.setInvitedRole(invitedRole);
        invitation.setInvitedBy(appUserRepository.getReferenceById(invitedByUserId));
        invitation.setStatus(BusinessInvitationStatus.PENDING);
        invitation.setTokenHash(hashToken(rawToken));
        invitation.setExpiresAt(Instant.now().plusSeconds(invitationTtlSeconds));
        invitation.setBranchIds(branchIds == null
                ? new LinkedHashSet<>()
                : new LinkedHashSet<>(branchIds));

        BusinessInvitation saved = businessInvitationRepository.save(invitation);
        return CreatedBusinessInvitationDto.builder()
                .invitation(businessInvitationMapper.toDto(saved))
                .rawToken(rawToken)
                .build();
    }

    @Override
    @Transactional
    public List<BusinessInvitationDto> findByBusiness(UUID businessId) {
        return businessInvitationRepository.findByBusinessIdOrderByCreatedAtDesc(businessId).stream()
                .map(this::expireIfNeeded)
                .map(businessInvitationMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public List<BusinessInvitationDto> findPendingByEmail(String email) {
        return businessInvitationRepository
                .findByInvitedEmailIgnoreCaseAndStatusOrderByCreatedAtDesc(
                        email,
                        BusinessInvitationStatus.PENDING)
                .stream()
                .map(this::expireIfNeeded)
                .filter(invitation -> invitation.getStatus() == BusinessInvitationStatus.PENDING)
                .map(businessInvitationMapper::toDto)
                .toList();
    }

    @Override
    public Long countPendingByEmail(String email) {
        return businessInvitationRepository.countByInvitedEmailIgnoreCaseAndStatusAndExpiresAtAfter(
                email,
                BusinessInvitationStatus.PENDING,
                Instant.now());
    }

    @Override
    public BusinessInvitationDto findById(UUID invitationId) {
        return businessInvitationMapper.toDto(requireInvitation(invitationId));
    }

    @Override
    @Transactional
    public BusinessInvitationDto accept(UUID invitationId, UUID acceptedByUserId) {
        BusinessInvitation invitation = requirePending(invitationId);
        invitation.setStatus(BusinessInvitationStatus.ACCEPTED);
        invitation.setAcceptedBy(appUserRepository.getReferenceById(acceptedByUserId));
        invitation.setAcceptedAt(Instant.now());
        return businessInvitationMapper.toDto(invitation);
    }

    @Override
    @Transactional
    public BusinessInvitationDto decline(UUID invitationId) {
        BusinessInvitation invitation = requirePending(invitationId);
        invitation.setStatus(BusinessInvitationStatus.DECLINED);
        invitation.setDeclinedAt(Instant.now());
        return businessInvitationMapper.toDto(invitation);
    }

    @Override
    @Transactional
    public BusinessInvitationDto revoke(UUID invitationId) {
        BusinessInvitation invitation = requirePending(invitationId);
        invitation.setStatus(BusinessInvitationStatus.REVOKED);
        invitation.setRevokedAt(Instant.now());
        return businessInvitationMapper.toDto(invitation);
    }

    private BusinessInvitation requirePending(UUID invitationId) {
        BusinessInvitation invitation = expireIfNeeded(requireInvitation(invitationId));
        if (invitation.getStatus() != BusinessInvitationStatus.PENDING) {
            throw new ValidationException(ErrorCode.INVITATION_NOT_PENDING);
        }
        return invitation;
    }

    private BusinessInvitation requireInvitation(UUID invitationId) {
        return businessInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.INVITATION_NOT_FOUND));
    }

    private BusinessInvitation expireIfNeeded(BusinessInvitation invitation) {
        if (invitation.getStatus() == BusinessInvitationStatus.PENDING
                && invitation.getExpiresAt().isBefore(Instant.now())) {
            invitation.setStatus(BusinessInvitationStatus.EXPIRED);
        }
        return invitation;
    }

    private String createRawToken() {
        byte[] bytes = new byte[tokenBytes];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new InternalServerException(ErrorCode.INTERNAL_ERROR);
        }
    }
}
