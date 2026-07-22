package kz.ask.item.infrastructure.repository;

import java.util.List;
import java.util.UUID;

import kz.ask.item.domain.entity.Item;
import kz.ask.item.domain.enums.ProductModerationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Item, UUID> {

    @Query("SELECT DISTINCT p.categoryLabel FROM Item p WHERE p.business.id = :businessId AND p.categoryLabel IS NOT NULL AND LOWER(p.categoryLabel) LIKE LOWER(CONCAT(:query, '%')) ORDER BY p.categoryLabel")
    List<String> findDistinctCategoryLabelsByBusiness(UUID businessId, String query);

    Page<Item> findByBusinessId(UUID businessId, Pageable pageable);

    Page<Item> findByModerationStatusOrderByCreatedAtAsc(ProductModerationStatus moderationStatus, Pageable pageable);

    Long countByBusinessId(UUID businessId);

    Long countByModerationStatus(ProductModerationStatus moderationStatus);

    @Query("SELECT p FROM Item p WHERE p.branch.id = :branchId AND LOWER(p.name) LIKE LOWER(:query)")
    Page<Item> searchByBranchAndName(UUID branchId, String query, Pageable pageable);

    Page<Item> findByBranchIdAndEnabled(UUID branchId, Boolean enabled, Pageable pageable);

    Page<Item> findByBranchId(UUID branchId, Pageable pageable);
}
