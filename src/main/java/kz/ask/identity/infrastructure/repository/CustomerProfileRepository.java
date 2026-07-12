package kz.ask.identity.infrastructure.repository;

import java.util.Optional;
import java.util.UUID;
import kz.ask.identity.domain.entity.CustomerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, UUID> {

    Optional<CustomerProfile> findByUserId(UUID userId);
}
