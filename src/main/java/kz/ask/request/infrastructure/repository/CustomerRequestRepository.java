package kz.ask.request.infrastructure.repository;

import java.util.List;
import java.util.UUID;
import kz.ask.request.domain.entity.CustomerRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRequestRepository extends JpaRepository<CustomerRequest, UUID> {

    List<CustomerRequest> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<CustomerRequest> findAllByOrderByCreatedAtDesc();
}
