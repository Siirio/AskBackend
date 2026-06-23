package kz.ask.service.infrastructure.repository;

import kz.ask.service.domain.entity.ServiceOffering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ServiceOfferingRepository extends JpaRepository<ServiceOffering, UUID> {

}
