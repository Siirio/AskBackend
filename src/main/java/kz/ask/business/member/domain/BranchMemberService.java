package kz.ask.business.member.domain;

import java.util.List;
import java.util.UUID;
import kz.ask.business.member.domain.dto.BranchMemberDto;
import kz.ask.identity.authorization.domain.enums.Role;

public interface BranchMemberService {

    BranchMemberDto addMember(UUID branchId, UUID userId, Role role);

    BranchMemberDto updateMemberRole(UUID memberId, Role role);

    List<BranchMemberDto> findByBranch(UUID branchId);

    Boolean isStaffOfBranch(UUID branchId, UUID userId);

    Boolean isBranchStaff(UUID userId);

    BranchMemberDto findByUser(UUID userId);

    void removeByUser(UUID userId);
}
