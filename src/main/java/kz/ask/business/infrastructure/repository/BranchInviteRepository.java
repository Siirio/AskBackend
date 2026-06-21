package kz.ask.business.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kz.ask.business.domain.entity.BranchInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchInviteRepository extends JpaRepository<BranchInvite, UUID> {

    List<BranchInvite> findByBranchIdAndRevokedAtIsNull(UUID branchId);

    Optional<BranchInvite> findByCode(String code);
}
