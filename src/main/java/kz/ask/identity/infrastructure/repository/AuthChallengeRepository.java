package kz.ask.identity.infrastructure.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import kz.ask.identity.domain.entity.AuthChallenge;
import kz.ask.identity.domain.enums.AuthChallengeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthChallengeRepository extends JpaRepository<AuthChallenge, UUID> {

    Optional<AuthChallenge> findByIdAndStatus(UUID id, AuthChallengeStatus status);

    @Modifying
    @Query("UPDATE AuthChallenge c SET c.status = :expiredStatus WHERE c.status = :pendingStatus AND c.expiresAt < :now")
    void expirePendingChallenges(Instant now, AuthChallengeStatus expiredStatus, AuthChallengeStatus pendingStatus);

    void deleteByUserId(UUID userId);

    void deleteByExpiresAtBefore(Instant cutoff);
}
