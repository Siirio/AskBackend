package kz.ask.business.domain;

import java.util.List;
import java.util.UUID;

public interface BusinessMemberBranchService {

    void assign(UUID businessMembershipId, UUID branchId);

    List<UUID> findBranchIds(UUID businessMembershipId);
}
