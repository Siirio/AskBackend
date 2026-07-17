package kz.ask.managedimport.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kz.ask.managedimport.domain.entity.ManagedImportRequest;
import kz.ask.managedimport.domain.enums.ManagedImportStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManagedImportRequestRepository
        extends JpaRepository<ManagedImportRequest, UUID> {

    @EntityGraph(attributePaths = {
            "business", "requestedBy", "responsiblePlatformUser", "selectedSourceTypes"
    })
    List<ManagedImportRequest> findByStatusInOrderByCreatedAtAsc(
            List<ManagedImportStatus> statuses);

    @Override
    @EntityGraph(attributePaths = {
            "business", "requestedBy", "responsiblePlatformUser", "selectedSourceTypes"
    })
    Optional<ManagedImportRequest> findById(UUID id);

    @EntityGraph(attributePaths = {"selectedSourceTypes"})
    List<ManagedImportRequest> findByBusinessIdOrderByCreatedAtDesc(UUID businessId);
}
