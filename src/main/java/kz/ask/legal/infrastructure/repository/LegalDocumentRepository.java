package kz.ask.legal.infrastructure.repository;

import java.util.List;
import java.util.UUID;
import kz.ask.legal.domain.entity.LegalDocument;
import kz.ask.legal.domain.enums.LegalDocumentCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LegalDocumentRepository extends JpaRepository<LegalDocument, UUID> {

    List<LegalDocument> findByCountryCodeAndIsActiveTrueOrderByCodeAsc(String countryCode);

    List<LegalDocument> findByCountryCodeAndCodeInAndIsActiveTrue(
            String countryCode,
            List<LegalDocumentCode> codes);
}
