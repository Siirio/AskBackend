package kz.ask.business.core.infrastructure.repository;

import java.util.UUID;
import kz.ask.business.core.domain.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessRepository extends JpaRepository<Business, UUID> {

    @Query("""
        SELECT b FROM Business b
        WHERE :query = ''
           OR LOWER(b.name) LIKE :query
           OR LOWER(COALESCE(b.legalName, '')) LIKE :query
        """)
    Page<Business> searchForPlatform(String query, Pageable pageable);
}
