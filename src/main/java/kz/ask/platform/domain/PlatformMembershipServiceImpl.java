package kz.ask.platform.domain;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.identity.infrastructure.repository.AppUserRepository;
import kz.ask.platform.domain.dto.PlatformMembershipDto;
import kz.ask.platform.domain.entity.PlatformMembership;
import kz.ask.platform.domain.enums.PlatformPermission;
import kz.ask.platform.domain.enums.PlatformRole;
import kz.ask.platform.infrastructure.mapper.PlatformMembershipMapper;
import kz.ask.platform.infrastructure.repository.PlatformMembershipRepository;

import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.NotFoundException;
import kz.ask.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlatformMembershipServiceImpl implements PlatformMembershipService {

    private final PlatformMembershipRepository platformMembershipRepository;
    private final PlatformMembershipMapper platformMembershipMapper;
    private final AppUserRepository appUserRepository;

    @Override
    @Transactional(readOnly = true)
    public PlatformMembershipDto findActiveByUser(UUID userId) {
        return platformMembershipRepository.findByUserId(userId)
                .map(platformMembershipMapper::toDto)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlatformMembershipDto> listAll() {
        return platformMembershipMapper.toDtoList(
                platformMembershipRepository.findAllByOrderByCreatedAtDesc());
    }

    @Override
    @Transactional
    public PlatformMembershipDto create(UUID userId, PlatformRole role, Set<PlatformPermission> permissions) {
        if (platformMembershipRepository.findByUserId(userId).isPresent()) {
            throw new ValidationException(ErrorCode.PLATFORM_MEMBERSHIP_EXISTS);
        }
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
        PlatformMembership membership = new PlatformMembership();
        membership.setUser(user);
        membership.setRole(role);
        membership.setPermissions(new LinkedHashSet<>(permissions));
        return platformMembershipMapper.toDto(platformMembershipRepository.save(membership));
    }

    @Override
    @Transactional
    public PlatformMembershipDto update(UUID membershipId, PlatformRole role, Set<PlatformPermission> permissions) {
        PlatformMembership membership = platformMembershipRepository.findById(membershipId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PLATFORM_MEMBERSHIP_NOT_FOUND));
        if (role != null) {
            membership.setRole(role);
        }
        if (permissions != null) {
            membership.getPermissions().clear();
            membership.getPermissions().addAll(permissions);
        }
        return platformMembershipMapper.toDto(platformMembershipRepository.save(membership));
    }

    @Override
    @Transactional
    public PlatformMembershipDto deactivate(UUID membershipId) {
        PlatformMembership membership = platformMembershipRepository.findById(membershipId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PLATFORM_MEMBERSHIP_NOT_FOUND));
        platformMembershipRepository.delete(membership);
        return platformMembershipMapper.toDto(membership);
    }

    @Override
    @Transactional
    public void delete(UUID membershipId) {
        PlatformMembership membership = platformMembershipRepository.findById(membershipId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PLATFORM_MEMBERSHIP_NOT_FOUND));
        platformMembershipRepository.delete(membership);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByRole(PlatformRole role) {
        return platformMembershipRepository.countByRole(role);
    }
}
