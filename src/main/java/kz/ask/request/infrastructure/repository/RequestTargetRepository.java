package kz.ask.request.infrastructure.repository;

import java.util.List;
import java.util.UUID;
import kz.ask.request.domain.entity.RequestTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestTargetRepository extends JpaRepository<RequestTarget, UUID> {

    List<RequestTarget> findByBranchIdOrderByCreatedAtDesc(UUID branchId);
}
