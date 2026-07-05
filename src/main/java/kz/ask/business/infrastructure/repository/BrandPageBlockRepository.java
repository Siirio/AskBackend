package kz.ask.business.infrastructure.repository;

import java.util.List;
import java.util.UUID;
import kz.ask.business.domain.entity.BrandPageBlock;
import kz.ask.business.domain.enums.StorefrontPageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrandPageBlockRepository extends JpaRepository<BrandPageBlock, UUID> {
    List<BrandPageBlock> findByBusinessIdAndEnabledOrderByDisplayOrderAsc(UUID businessId, Boolean enabled);

    List<BrandPageBlock> findByBusinessIdAndPageStatusAndEnabledOrderByDisplayOrderAsc(UUID businessId,
                                                                                       StorefrontPageStatus pageStatus,
                                                                                       Boolean enabled);

    List<BrandPageBlock> findByBusinessIdAndPageStatusOrderByDisplayOrderAsc(UUID businessId,
                                                                              StorefrontPageStatus pageStatus);

    List<BrandPageBlock> findByBusinessIdOrderByDisplayOrderAsc(UUID businessId);

    void deleteByBusinessId(UUID businessId);

    void deleteByBusinessIdAndPageStatus(UUID businessId, StorefrontPageStatus pageStatus);
}
