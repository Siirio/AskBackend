package kz.ask.catalog.infrastructure.repository;

import java.util.UUID;
import kz.ask.catalog.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
}
