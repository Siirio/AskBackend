package kz.ask.business.infrastructure.repository;

import java.util.List;
import java.util.UUID;
import java.util.Collection;
import kz.ask.business.domain.entity.BusinessContact;
import kz.ask.shared.domain.enums.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessContactRepository extends JpaRepository<BusinessContact, UUID> {
    List<BusinessContact> findByBusinessIdAndStatusOrderByPrimaryContactDescUpdatedAtDesc(UUID businessId,
                                                                                          RecordStatus status);

    List<BusinessContact> findByBusinessIdInAndStatusOrderByPrimaryContactDescUpdatedAtDesc(
            Collection<UUID> businessIds, RecordStatus status);
}
