package kz.ask.business.profile.infrastructure.repository;

import java.util.Optional;
import java.util.UUID;
import java.util.Collection;
import java.util.List;
import kz.ask.business.profile.domain.entity.BusinessProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessProfileRepository extends JpaRepository<BusinessProfile, UUID> {
    Optional<BusinessProfile> findByBusinessId(UUID businessId);

    List<BusinessProfile> findByBusinessIdIn(Collection<UUID> businessIds);
}
