package kz.ask.service.infrastructure.repository;

import java.util.List;
import java.util.UUID;
import kz.ask.service.domain.entity.ServiceOffering;
import kz.ask.shared.domain.enums.RecordStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceOfferingRepository extends JpaRepository<ServiceOffering, UUID> {

    @Query("SELECT DISTINCT s.categoryLabel FROM ServiceOffering s WHERE s.business.id = :businessId AND s.categoryLabel IS NOT NULL AND LOWER(s.categoryLabel) LIKE LOWER(CONCAT(:query, '%')) ORDER BY s.categoryLabel")
    List<String> findDistinctCategoryLabelsByBusiness(UUID businessId, String query);

    Long countByStatus(RecordStatus status);

    Page<ServiceOffering> findByBusinessId(UUID businessId, Pageable pageable);

    Long countByBusinessIdAndStatus(UUID businessId, RecordStatus status);
}
