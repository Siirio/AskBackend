package kz.ask.service.infrastructure.repository;

import java.util.List;
import java.util.UUID;
import kz.ask.service.domain.entity.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceOfferingRepository extends JpaRepository<Service, UUID> {

    @Query("SELECT DISTINCT s.categoryLabel FROM Service s WHERE s.business.id = :businessId AND s.categoryLabel IS NOT NULL AND LOWER(s.categoryLabel) LIKE LOWER(CONCAT(:query, '%')) ORDER BY s.categoryLabel")
    List<String> findDistinctCategoryLabelsByBusiness(UUID businessId, String query);

    Long countByBusinessId(UUID businessId);

    Page<Service> findByBusinessId(UUID businessId, Pageable pageable);

    @Query("""
        SELECT so FROM Service so
        WHERE (:branchId IS NULL OR so.branch.id = :branchId)
          AND (:categoryLabel IS NULL OR LOWER(so.categoryLabel) = LOWER(:categoryLabel))
          AND (:active IS NULL OR so.active = :active)
          AND (:query IS NULL OR LOWER(so.name) LIKE :query OR LOWER(COALESCE(so.description, '')) LIKE :query)
        """)
    Page<Service> search(UUID branchId, String categoryLabel, Boolean active, String query, Pageable pageable);
}
