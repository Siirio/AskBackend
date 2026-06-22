package kz.ask.business.infrastructure.repository;

import java.util.Optional;
import java.util.UUID;
import kz.ask.business.domain.entity.DataSource;
import kz.ask.business.domain.enums.DataSourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DataSourceRepository extends JpaRepository<DataSource, UUID> {

    Optional<DataSource> findByBusinessIdAndSourceType(UUID businessId, DataSourceType sourceType);
}
