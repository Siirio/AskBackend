package kz.ask.catalog.infrastructure.repository;

import java.util.UUID;
import kz.ask.catalog.domain.entity.ProductOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductOfferRepository extends JpaRepository<ProductOffer, UUID> {
}
