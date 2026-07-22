package kz.ask.business.infrastructure.repository;

import java.util.List;
import java.util.UUID;
import kz.ask.business.domain.entity.BusinessBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessBranchRepository extends JpaRepository<BusinessBranch, UUID> {

    List<BusinessBranch> findByBusinessId(UUID businessId);

    List<BusinessBranch> findByCityId(UUID cityId);
}
