package kz.ask.platform.domain;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import kz.ask.platform.domain.dto.PlatformMembershipDto;
import kz.ask.identity.authorization.domain.enums.Permission;
import kz.ask.identity.authorization.domain.enums.Role;

public interface PlatformMembershipService {

    PlatformMembershipDto findActiveByUser(UUID userId);

    List<PlatformMembershipDto> listAll();

    PlatformMembershipDto create(UUID userId, Role role, Set<Permission> permissions);

    PlatformMembershipDto update(UUID membershipId, Role role, Set<Permission> permissions);

    PlatformMembershipDto deactivate(UUID membershipId);

    void delete(UUID membershipId);

    long countByRole(Role role);
}
