package kz.ask.service.infrastructure.repository;

import kz.ask.service.domain.entity.ServiceBranchOffer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceBranchOfferRepository extends JpaRepository<ServiceBranchOffer,UUID>, JpaSpecificationExecutor<ServiceBranchOffer> {
    Page<ServiceBranchOffer> findByBranchId(UUID branchId, Pageable pageable);
    Optional<ServiceBranchOffer> findByServiceOfferingIdAndBranchId(UUID serviceOfferingId, UUID branchId);
}
