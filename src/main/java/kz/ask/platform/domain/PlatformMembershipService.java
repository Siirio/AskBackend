package kz.ask.platform.domain;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import kz.ask.platform.domain.dto.PlatformMembershipDto;
import kz.ask.platform.domain.enums.PlatformPermission;
import kz.ask.platform.domain.enums.PlatformRole;

public interface PlatformMembershipService {

    PlatformMembershipDto findActiveByUser(UUID userId);

    List<PlatformMembershipDto> listAll();

    PlatformMembershipDto create(UUID userId, PlatformRole role, Set<PlatformPermission> permissions);

    PlatformMembershipDto update(UUID membershipId, PlatformRole role, Set<PlatformPermission> permissions);

    PlatformMembershipDto deactivate(UUID membershipId);

    void delete(UUID membershipId);

    long countByRole(PlatformRole role);
}
