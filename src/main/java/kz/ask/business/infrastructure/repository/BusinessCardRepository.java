package kz.ask.business.infrastructure.repository;

import java.util.Optional;
import java.util.UUID;
import kz.ask.business.domain.entity.BusinessCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessCardRepository extends JpaRepository<BusinessCard, UUID> {
    Optional<BusinessCard> findByBusinessId(UUID businessId);
}
