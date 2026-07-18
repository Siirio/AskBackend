package kz.ask.identity.infrastructure.repository;

import java.util.List;
import java.util.UUID;
import kz.ask.identity.domain.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    List<AppUser> findAllByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
