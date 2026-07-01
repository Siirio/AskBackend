package kz.ask.business.infrastructure.repository;

import java.util.Optional;
import java.util.UUID;
import kz.ask.business.domain.entity.BrandProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrandProfileRepository extends JpaRepository<BrandProfile, UUID> {
    Optional<BrandProfile> findByBusinessId(UUID businessId);
}
