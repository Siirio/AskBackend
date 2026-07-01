package kz.ask.business.infrastructure.repository;

import java.util.List;
import java.util.UUID;
import kz.ask.business.domain.entity.BrandPageBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrandPageBlockRepository extends JpaRepository<BrandPageBlock, UUID> {
    List<BrandPageBlock> findByBusinessIdAndEnabledOrderByDisplayOrderAsc(UUID businessId, Boolean enabled);

    List<BrandPageBlock> findByBusinessIdOrderByDisplayOrderAsc(UUID businessId);

    void deleteByBusinessId(UUID businessId);
}
