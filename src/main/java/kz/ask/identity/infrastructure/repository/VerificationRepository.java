package kz.ask.identity.infrastructure.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import kz.ask.identity.domain.entity.Verification;
import kz.ask.identity.domain.enums.VerificationPurpose;
import kz.ask.identity.domain.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationRepository extends JpaRepository<Verification, UUID> {

    Optional<Verification> findByIdAndStatus(UUID id, VerificationStatus status);

    @Modifying
    @Query("UPDATE Verification c SET c.status = :expiredStatus WHERE c.status = :pendingStatus AND c.expiresAt < :now")
    void expirePendingChallenges(Instant now, VerificationStatus expiredStatus, VerificationStatus pendingStatus);

    @Modifying
    @Query("""
        UPDATE Verification c
        SET c.status = :expiredStatus
        WHERE c.user.id = :userId
          AND c.purpose = :purpose
          AND c.status = :pendingStatus
        """)
    void expirePendingChallengesForUser(
            UUID userId,
            VerificationPurpose purpose,
            VerificationStatus expiredStatus,
            VerificationStatus pendingStatus);

    void deleteByUserId(UUID userId);

    void deleteByExpiresAtBefore(Instant cutoff);
}
