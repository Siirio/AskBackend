package kz.ask.identity.infrastructure.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import kz.ask.identity.domain.entity.AuthSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthSessionRepository extends JpaRepository<AuthSession, UUID> {

    @Query("""
        select s from AuthSession s
        join fetch s.user u
        where s.tokenHash = :tokenHash
        """)
    Optional<AuthSession> findByTokenHash(String tokenHash);

    @Modifying
    @Query("UPDATE AuthSession s SET s.revokedAt = :now WHERE s.user.id = :userId AND s.revokedAt IS NULL")
    void revokeAllForUser(UUID userId, Instant now);

    @Modifying
    @Query("""
        UPDATE AuthSession s
        SET s.revokedAt = :now
        WHERE s.user.id = :userId
          AND s.id <> :currentSessionId
          AND s.revokedAt IS NULL
        """)
    void revokeOtherSessions(UUID userId, UUID currentSessionId, Instant now);

    void deleteByUserId(UUID userId);

    @Modifying
    @Query("UPDATE AuthSession s SET s.revokedAt = :now WHERE s.expiresAt < :now AND s.revokedAt IS NULL")
    void revokeExpiredSessions(Instant now);
}
