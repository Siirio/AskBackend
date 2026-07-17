package kz.ask.business.infrastructure.repository;

import java.util.List;
import java.util.UUID;
import kz.ask.business.domain.entity.BusinessMemberBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessMemberBranchRepository extends JpaRepository<BusinessMemberBranch, UUID> {

    List<BusinessMemberBranch> findByBusinessMembershipId(UUID businessMembershipId);

    Boolean existsByBusinessMembershipIdAndBranchId(UUID businessMembershipId, UUID branchId);

    void deleteByBusinessMembershipId(UUID businessMembershipId);
}
