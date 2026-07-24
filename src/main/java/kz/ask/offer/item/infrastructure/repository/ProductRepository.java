package kz.ask.offer.item.infrastructure.repository;

import java.util.List;
import java.util.UUID;

import kz.ask.offer.item.domain.entity.Item;
import kz.ask.offer.item.domain.enums.ProductModerationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Item, UUID> {

    @Query("SELECT DISTINCT p.category.name FROM Item p WHERE p.business.id = :businessId AND LOWER(p.category.name) LIKE LOWER(CONCAT(:query, '%')) ORDER BY p.category.name")
    List<String> findDistinctCategoryLabelsByBusiness(UUID businessId, String query);

    Page<Item> findByBusinessId(UUID businessId, Pageable pageable);

    Page<Item> findByModerationStatusOrderByCreatedAtAsc(ProductModerationStatus moderationStatus, Pageable pageable);

    Long countByBusinessId(UUID businessId);

    Long countByModerationStatus(ProductModerationStatus moderationStatus);

    @Query("SELECT p FROM Item p WHERE p.branch.id = :branchId AND LOWER(p.name) LIKE LOWER(:query)")
    Page<Item> searchByBranchAndName(UUID branchId, String query, Pageable pageable);

    Page<Item> findByBranchIdAndIsActive(UUID branchId, Boolean isActive, Pageable pageable);

    Page<Item> findByBranchId(UUID branchId, Pageable pageable);

    @Query("SELECT p FROM Item p WHERE p.business.id = :businessId AND LOWER(p.name) LIKE LOWER(:query)")
    Page<Item> searchByBusinessAndName(UUID businessId, String query, Pageable pageable);

    Page<Item> findByBusinessIdAndIsActive(UUID businessId, Boolean isActive, Pageable pageable);

    List<Item> findByIdIn(List<UUID> ids);
}
