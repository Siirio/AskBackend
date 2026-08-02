package kz.ask.offer.service.infrastructure.repository;

import java.util.List;
import java.util.UUID;
import kz.ask.offer.service.domain.entity.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceOfferingRepository extends JpaRepository<Service, UUID> {

    @Query("SELECT DISTINCT s.category.name FROM Service s WHERE s.business.id = :businessId AND LOWER(s.category.name) LIKE LOWER(CONCAT(:query, '%')) ORDER BY s.category.name")
    List<String> findDistinctCategoryLabelsByBusiness(UUID businessId, String query);

    Long countByBusinessId(UUID businessId);

    Long countByIsActive(Boolean isActive);

    Long countByIdInAndBusinessId(List<UUID> ids, UUID businessId);

    Page<Service> findByBusinessId(UUID businessId, Pageable pageable);

    List<Service> findAllByBusinessId(UUID businessId);

    @Query("""
        SELECT so FROM Service so
        WHERE so.business.id = :businessId
          AND (:branchId IS NULL OR so.branch.id = :branchId)
          AND (:categoryLabel = '' OR LOWER(so.category.name) = :categoryLabel)
          AND (:active IS NULL OR so.isActive = :active)
          AND (:query = '' OR LOWER(so.name) LIKE :query OR LOWER(COALESCE(so.description, '')) LIKE :query)
        """)
    Page<Service> search(UUID businessId, UUID branchId, String categoryLabel, Boolean active,
                          String query, Pageable pageable);

    @EntityGraph(attributePaths = "imageFiles")
    List<Service> findByIdIn(List<UUID> ids);
}
