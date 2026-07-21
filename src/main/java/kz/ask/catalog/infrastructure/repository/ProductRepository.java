package kz.ask.catalog.infrastructure.repository;

import java.util.List;
import java.util.UUID;
import kz.ask.catalog.domain.entity.Product;
import kz.ask.catalog.domain.enums.ProductModerationStatus;
import kz.ask.shared.domain.enums.RecordStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    boolean existsByBusinessIdAndSkuIgnoreCase(UUID businessId, String sku);

    boolean existsByBusinessIdAndSkuIgnoreCaseAndIdNot(UUID businessId, String sku, UUID excludedProductId);

    @Query("SELECT DISTINCT p.categoryLabel FROM Product p WHERE p.business.id = :businessId AND p.categoryLabel IS NOT NULL AND LOWER(p.categoryLabel) LIKE LOWER(CONCAT(:query, '%')) ORDER BY p.categoryLabel")
    List<String> findDistinctCategoryLabelsByBusiness(UUID businessId, String query);

    Long countByStatus(RecordStatus status);

    Page<Product> findByBusinessId(UUID businessId, Pageable pageable);

    Page<Product> findByModerationStatusOrderByCreatedAtAsc(ProductModerationStatus moderationStatus, Pageable pageable);

    Long countByModerationStatus(ProductModerationStatus moderationStatus);

    Long countByBusinessIdAndStatus(UUID businessId, RecordStatus status);
}
