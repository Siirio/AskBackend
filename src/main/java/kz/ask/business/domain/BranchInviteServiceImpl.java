package kz.ask.business.domain;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import kz.ask.business.domain.dto.BranchInviteDto;
import kz.ask.business.domain.entity.BranchInvite;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.business.domain.enums.BranchMemberRole;
import kz.ask.business.infrastructure.mapper.BusinessMapper;
import kz.ask.business.infrastructure.repository.BranchInviteRepository;
import kz.ask.business.infrastructure.repository.BusinessBranchRepository;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.identity.infrastructure.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BranchInviteServiceImpl implements BranchInviteService {

    private final BranchInviteRepository branchInviteRepository;
    private final BusinessBranchRepository businessBranchRepository;
    private final AppUserRepository appUserRepository;
    private final BusinessMapper businessMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public BranchInviteDto create(UUID branchId, BranchMemberRole role, Integer maxUses,
                                   Long ttlSeconds, UUID createdBy) {
        BusinessBranch branch = businessBranchRepository.getReferenceById(branchId);
        AppUser createdByUser = appUserRepository.getReferenceById(createdBy);
        String code = generateInviteCode();
        BranchInvite entity = branchInviteRepository.save(
                businessMapper.toInviteEntity(branch, role, maxUses,
                        Instant.now().plusSeconds(ttlSeconds), createdByUser, code));
        return businessMapper.toBranchInviteDto(entity);
    }

    @Override
    public List<BranchInviteDto> findByBranch(UUID branchId) {
        List<BranchInvite> entities = branchInviteRepository.findByBranchIdAndRevokedAtIsNull(branchId);
        return businessMapper.toBranchInviteDtoList(entities);
    }

    @Override
    @Transactional
    public void revoke(UUID inviteId) {
        branchInviteRepository.findById(inviteId).ifPresent(invite -> {
            invite.setRevokedAt(Instant.now());
        });
    }

    private String generateInviteCode() {
        byte[] bytes = new byte[16];
        secureRandom.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
