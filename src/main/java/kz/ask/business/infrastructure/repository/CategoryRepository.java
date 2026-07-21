package kz.ask.business.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kz.ask.business.domain.entity.Category;
import kz.ask.shared.domain.enums.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findByParentIsNullAndStatus(RecordStatus status);

    List<Category> findByParentIdAndStatus(UUID parentId, RecordStatus status);

    Optional<Category> findByNameIgnoreCase(String name);

    @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT(:query, '%')) AND c.status = :status ORDER BY c.name")
    List<Category> findByNameStartingWithIgnoreCaseAndStatus(String query, RecordStatus status);
}
