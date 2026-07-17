package kz.ask.business.infrastructure.repository;

import java.util.Optional;
import java.util.UUID;
import kz.ask.business.domain.entity.BusinessDeliveryProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessDeliveryProfileRepository
        extends JpaRepository<BusinessDeliveryProfile, UUID> {

    Optional<BusinessDeliveryProfile> findByBusinessId(UUID businessId);
}
