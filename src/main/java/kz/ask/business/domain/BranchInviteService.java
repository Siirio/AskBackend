package kz.ask.business.domain;

import java.util.List;
import java.util.UUID;
import kz.ask.business.domain.dto.BranchInviteDto;
import kz.ask.business.domain.enums.BranchMemberRole;

public interface BranchInviteService {

    BranchInviteDto create(UUID branchId, BranchMemberRole role, Integer maxUses, Long ttlSeconds, UUID createdBy);

    List<BranchInviteDto> findByBranch(UUID branchId);

    void revoke(UUID inviteId);
}
