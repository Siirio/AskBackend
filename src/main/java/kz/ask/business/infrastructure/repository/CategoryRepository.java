package kz.ask.business.infrastructure.repository;

import java.util.UUID;
import kz.ask.business.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
}
