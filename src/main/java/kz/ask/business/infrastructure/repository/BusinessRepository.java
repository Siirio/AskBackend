package kz.ask.business.infrastructure.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.enums.CatalogStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessRepository extends JpaRepository<Business, UUID> {

    List<Business> findByCatalogStatusAndCatalogDeadlineAtBefore(
            CatalogStatus catalogStatus,
            Instant deadline);

    List<Business> findByCatalogStatusOrderByCreatedAtAsc(CatalogStatus catalogStatus);
}
