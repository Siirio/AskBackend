package kz.ask.request.infrastructure.repository;

import java.util.Optional;
import java.util.UUID;
import kz.ask.request.domain.entity.SupplierResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierResponseRepository extends JpaRepository<SupplierResponse, UUID> {

    Optional<SupplierResponse> findTopByRequestTarget_IdOrderByCreatedAtDesc(UUID requestTargetId);
}
