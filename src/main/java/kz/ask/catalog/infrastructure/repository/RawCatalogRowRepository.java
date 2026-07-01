package kz.ask.catalog.infrastructure.repository;

import java.util.List;
import java.util.UUID;
import kz.ask.catalog.domain.entity.RawCatalogRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RawCatalogRowRepository extends JpaRepository<RawCatalogRow, UUID> {

    List<RawCatalogRow> findByCatalogImportIdOrderByRowNumberAsc(UUID catalogImportId);
}
