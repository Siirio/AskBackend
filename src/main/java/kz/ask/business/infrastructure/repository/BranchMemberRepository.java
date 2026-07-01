package kz.ask.business.infrastructure.repository;

import java.util.List;
import java.util.UUID;
import kz.ask.business.domain.entity.BranchMember;
import kz.ask.shared.domain.enums.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchMemberRepository extends JpaRepository<BranchMember, UUID> {

    List<BranchMember> findByBranchIdAndStatus(UUID branchId, RecordStatus status);

    List<BranchMember> findByUserIdAndStatus(UUID userId, RecordStatus status);
}
