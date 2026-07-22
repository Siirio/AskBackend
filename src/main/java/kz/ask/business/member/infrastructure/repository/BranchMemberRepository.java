package kz.ask.business.member.infrastructure.repository;

import java.util.List;
import java.util.UUID;
import kz.ask.business.member.domain.entity.BranchMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchMemberRepository extends JpaRepository<BranchMember, UUID> {

    List<BranchMember> findByBranchId(UUID branchId);

    List<BranchMember> findByUserId(UUID userId);
}
