package kz.ask.managedimport.infrastructure.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import kz.ask.managedimport.domain.entity.ManagedImportGrant;
import kz.ask.managedimport.domain.enums.ManagedImportGrantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ManagedImportGrantRepository
        extends JpaRepository<ManagedImportGrant, UUID> {

    Optional<ManagedImportGrant> findByManagedImportRequestIdAndStatus(
            UUID managedImportRequestId,
            ManagedImportGrantStatus status);

    @Query("""
        select g from ManagedImportGrant g
        where g.business.id = :businessId
          and g.managedImportRequest.responsiblePlatformUser.id = :platformUserId
          and g.managedImportRequest.status = kz.ask.managedimport.domain.enums.ManagedImportStatus.ACTIVE
          and g.managedImportRequest.expiresAt > :now
          and g.status = kz.ask.managedimport.domain.enums.ManagedImportGrantStatus.ACTIVE
        """)
    Optional<ManagedImportGrant> findActiveAccess(
            @Param("businessId") UUID businessId,
            @Param("platformUserId") UUID platformUserId,
            @Param("now") Instant now);
}
