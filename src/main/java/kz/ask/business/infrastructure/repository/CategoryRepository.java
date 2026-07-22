package kz.ask.business.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kz.ask.business.domain.entity.Category;
import kz.ask.business.domain.enums.CategoryScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findByParentIsNull();

    List<Category> findByParentId(UUID parentId);

    Optional<Category> findByNameIgnoreCase(String name);

    @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT(:query, '%')) ORDER BY c.name")
    List<Category> findByNameStartingWithIgnoreCase(String query);

    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND (c.scope = :scope OR c.scope = 'BOTH') ORDER BY c.name")
    List<Category> findRootsByScope(CategoryScope scope);

    @Query("SELECT c FROM Category c WHERE c.parent IS NOT NULL AND (c.scope = :scope OR c.scope = 'BOTH') ORDER BY c.name")
    List<Category> findSubcategoriesByScope(CategoryScope scope);

    @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT(:query, '%')) AND (c.scope = :scope OR c.scope = 'BOTH') ORDER BY c.name")
    List<Category> findByNameStartingWithScope(String query, CategoryScope scope);

    @Query("SELECT c FROM Category c WHERE c.parent IS NULL ORDER BY c.name")
    List<Category> findRoots();

    @Query("SELECT c FROM Category c WHERE c.parent IS NOT NULL ORDER BY c.name")
    List<Category> findSubcategories();

    @Query("SELECT c FROM Category c JOIN CategoryAlias a ON a.category = c WHERE LOWER(a.alias) LIKE LOWER(CONCAT(:aliasQuery, '%')) AND (c.scope = :scope OR c.scope = 'BOTH') ORDER BY c.name")
    List<Category> findByAliasStartingWithAndScope(String aliasQuery, CategoryScope scope);

    @Query("SELECT a.alias FROM CategoryAlias a JOIN a.category c WHERE LOWER(a.alias) LIKE LOWER(CONCAT(:aliasQuery, '%')) AND (c.scope = :scope OR c.scope = 'BOTH')")
    List<String> findAliasStringsByScope(String aliasQuery, CategoryScope scope);
}
