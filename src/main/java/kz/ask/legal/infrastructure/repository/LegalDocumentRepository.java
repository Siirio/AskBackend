package kz.ask.legal.infrastructure.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kz.ask.legal.domain.entity.LegalDocument;
import kz.ask.legal.domain.enums.LegalDocumentCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LegalDocumentRepository extends JpaRepository<LegalDocument, UUID> {

    List<LegalDocument> findByCountryCodeAndLocaleAndActiveTrueAndEffectiveAtLessThanEqualOrderByCode(
            String countryCode, String locale, Instant effectiveAt);

    Optional<LegalDocument> findFirstByCodeAndCountryCodeAndLocaleAndActiveTrueAndEffectiveAtLessThanEqualOrderByEffectiveAtDesc(
            LegalDocumentCode code, String countryCode, String locale, Instant effectiveAt);
}
