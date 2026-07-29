package kz.ask.business.verification.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kz.ask.business.verification.domain.entity.BusinessVerification;
import kz.ask.business.verification.domain.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessVerificationRepository extends JpaRepository<BusinessVerification, UUID> {

    Optional<BusinessVerification> findByBusinessId(UUID businessId);

    List<BusinessVerification> findByStatusOrderByCreatedAtAsc(VerificationStatus status);
}
