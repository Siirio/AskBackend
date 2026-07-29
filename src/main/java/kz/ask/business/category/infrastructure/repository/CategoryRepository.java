package kz.ask.business.category.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kz.ask.business.category.domain.entity.Category;
import kz.ask.business.category.domain.enums.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    @Modifying
    @Query(value = """
        insert into category (id, created_at, updated_at, name, slug, type, source)
        values (:id, :now, :now, :name, :slug, :type, :source)
        on conflict (name, type) do nothing
        """, nativeQuery = true)
    int insertIfAbsent(@Param("id") UUID id,
                       @Param("now") java.time.Instant now,
                       @Param("name") String name,
                       @Param("slug") String slug,
                       @Param("type") String type,
                       @Param("source") String source);

    Optional<Category> findByNameIgnoreCaseAndType(String name, CategoryType type);

    List<Category> findByTypeOrderByName(CategoryType type);

    List<Category> findByTypeAndNameStartingWithIgnoreCaseOrderByName(CategoryType type, String query);
}
