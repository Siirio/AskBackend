package kz.ask.catalog.infrastructure.repository;

import java.util.List;
import java.util.UUID;
import kz.ask.catalog.domain.entity.CatalogImportColumnMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CatalogImportColumnMappingRepository extends JpaRepository<CatalogImportColumnMapping, UUID> {

    List<CatalogImportColumnMapping> findByCatalogImportId(UUID catalogImportId);

    void deleteByCatalogImportId(UUID catalogImportId);
}
