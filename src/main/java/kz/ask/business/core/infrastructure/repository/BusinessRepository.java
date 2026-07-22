package kz.ask.business.core.infrastructure.repository;

import java.util.UUID;
import kz.ask.business.core.domain.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessRepository extends JpaRepository<Business, UUID> {
}
