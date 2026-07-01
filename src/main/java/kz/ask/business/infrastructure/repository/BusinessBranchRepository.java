package kz.ask.business.infrastructure.repository;

import java.util.List;
import java.util.UUID;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.shared.domain.enums.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessBranchRepository extends JpaRepository<BusinessBranch, UUID> {

    List<BusinessBranch> findByBusinessIdAndStatus(UUID businessId, RecordStatus status);

    List<BusinessBranch> findByCityIdAndStatus(UUID cityId, RecordStatus status);
}
