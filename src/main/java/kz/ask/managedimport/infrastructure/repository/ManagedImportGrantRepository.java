package kz.ask.managedimport.infrastructure.repository;

import java.util.Optional;
import java.util.UUID;
import kz.ask.managedimport.domain.entity.ManagedImportGrant;
import kz.ask.managedimport.domain.enums.ManagedImportGrantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManagedImportGrantRepository
        extends JpaRepository<ManagedImportGrant, UUID> {

    Optional<ManagedImportGrant> findByBusinessIdAndStatus(
            UUID businessId,
            ManagedImportGrantStatus status);

    Optional<ManagedImportGrant> findByManagedImportRequestIdAndStatus(
            UUID managedImportRequestId,
            ManagedImportGrantStatus status);
}
