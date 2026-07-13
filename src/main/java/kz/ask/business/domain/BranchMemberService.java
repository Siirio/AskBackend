package kz.ask.business.domain;

import java.util.List;
import java.util.UUID;
import kz.ask.business.domain.dto.BranchMemberDto;
import kz.ask.business.domain.enums.BranchMemberRole;

public interface BranchMemberService {

    BranchMemberDto addMember(UUID branchId, UUID userId, BranchMemberRole role);

    BranchMemberDto updateMemberRole(UUID memberId, BranchMemberRole role);

    List<BranchMemberDto> findByBranch(UUID branchId);

    Boolean isStaffOfBranch(UUID branchId, UUID userId);

    Boolean isBranchStaff(UUID userId);
}
