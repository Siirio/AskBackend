package kz.ask.business.infrastructure.repository;

import java.util.List;
import java.util.UUID;
import kz.ask.business.domain.entity.BrandDrop;
import kz.ask.business.domain.enums.BrandDropStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrandDropRepository extends JpaRepository<BrandDrop, UUID> {
    List<BrandDrop> findByBusinessIdAndStatusInOrderByStartDateDesc(UUID businessId, List<BrandDropStatus> statuses);

    List<BrandDrop> findByBusinessIdOrderByStartDateDesc(UUID businessId);
}
