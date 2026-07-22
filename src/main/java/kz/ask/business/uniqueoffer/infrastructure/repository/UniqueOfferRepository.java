package kz.ask.business.uniqueoffer.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kz.ask.business.uniqueoffer.domain.entity.UniqueOffer;
import kz.ask.business.uniqueoffer.domain.enums.UniqueOfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UniqueOfferRepository extends JpaRepository<UniqueOffer, UUID> {
    List<UniqueOffer> findByBusinessIdAndStatusInOrderByStartDateDesc(UUID businessId, List<UniqueOfferStatus> statuses);

    List<UniqueOffer> findByBusinessIdOrderByStartDateDesc(UUID businessId);

    Optional<UniqueOffer> findByIdAndBusinessId(UUID id, UUID businessId);

    List<UniqueOffer> findByStatusIn(List<UniqueOfferStatus> statuses);

    Long countByStatusIn(List<UniqueOfferStatus> statuses);

    Long countByBusinessIdAndStatusIn(UUID businessId, List<UniqueOfferStatus> statuses);

    List<UniqueOffer> findByIdIn(List<UUID> ids);
}
