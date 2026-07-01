package kz.ask.catalog.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kz.ask.catalog.domain.entity.CatalogImport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CatalogImportRepository extends JpaRepository<CatalogImport, UUID> {

    List<CatalogImport> findByBranchIdOrderByCreatedAtDesc(UUID branchId);

    Optional<CatalogImport> findByIdAndBranchId(UUID id, UUID branchId);
}
