package kz.ask.business.uniqueoffer.infrastructure.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kz.ask.business.uniqueoffer.domain.entity.UniqueOffer;
import kz.ask.business.uniqueoffer.domain.enums.UniqueOfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

    @Query("""
            SELECT DISTINCT offer
            FROM UniqueOffer offer
            JOIN FETCH offer.itemIds itemId
            WHERE itemId IN :itemIds
              AND offer.isActive = true
              AND offer.status = :status
              AND (offer.startDate IS NULL OR offer.startDate <= :now)
              AND (offer.endDate IS NULL OR offer.endDate >= :now)
            """)
    List<UniqueOffer> findActiveLinkedItems(
            List<UUID> itemIds,
            UniqueOfferStatus status,
            Instant now);

    @Query("""
            SELECT DISTINCT offer
            FROM UniqueOffer offer
            JOIN FETCH offer.serviceIds serviceId
            WHERE serviceId IN :serviceIds
              AND offer.isActive = true
              AND offer.status = :status
              AND (offer.startDate IS NULL OR offer.startDate <= :now)
              AND (offer.endDate IS NULL OR offer.endDate >= :now)
            """)
    List<UniqueOffer> findActiveLinkedServices(
            List<UUID> serviceIds,
            UniqueOfferStatus status,
            Instant now);

    @Query("""
            SELECT DISTINCT itemId
            FROM UniqueOffer offer
            JOIN offer.itemIds itemId
            WHERE offer.isActive = true
              AND offer.status = :status
              AND (offer.startDate IS NULL OR offer.startDate <= :now)
              AND (offer.endDate IS NULL OR offer.endDate >= :now)
            """)
    List<UUID> findAllActiveItemIds(UniqueOfferStatus status, Instant now);

    @Query("""
            SELECT DISTINCT serviceId
            FROM UniqueOffer offer
            JOIN offer.serviceIds serviceId
            WHERE offer.isActive = true
              AND offer.status = :status
              AND (offer.startDate IS NULL OR offer.startDate <= :now)
              AND (offer.endDate IS NULL OR offer.endDate >= :now)
            """)
    List<UUID> findAllActiveServiceIds(UniqueOfferStatus status, Instant now);
}
