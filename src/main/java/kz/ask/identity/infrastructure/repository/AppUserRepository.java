package kz.ask.identity.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.identity.domain.enums.AppRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    List<AppUser> findAllByEmailIgnoreCase(String email);

    Optional<AppUser> findByEmailIgnoreCaseAndRole(String email, AppRole role);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndRole(String email, AppRole role);
}
