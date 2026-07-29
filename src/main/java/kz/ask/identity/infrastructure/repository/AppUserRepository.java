package kz.ask.identity.infrastructure.repository;

import java.util.List;
import java.util.UUID;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.identity.domain.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    List<AppUser> findAllByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @Query("""
            SELECT u FROM AppUser u
            WHERE (:query = '' OR LOWER(COALESCE(u.email, '')) LIKE :query
                OR LOWER(COALESCE(u.displayName, '')) LIKE :query)
              AND (:status IS NULL OR u.status = :status)
            ORDER BY u.createdAt DESC
            """)
    Page<AppUser> searchForPlatform(String query, UserStatus status, Pageable pageable);
}
