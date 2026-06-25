package kz.ask.service.infrastructure.repository;

import java.util.UUID;
import kz.ask.service.domain.entity.ServiceOffering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceOfferingRepository extends JpaRepository<ServiceOffering, UUID> {
}
