package kz.ask.legal.infrastructure.repository;

import java.util.List;
import java.util.UUID;
import kz.ask.legal.domain.entity.LegalAcceptance;
import kz.ask.legal.domain.enums.LegalDocumentCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LegalAcceptanceRepository extends JpaRepository<LegalAcceptance, UUID> {

    boolean existsByUserIdAndDocumentCodeAndDocumentVersionAndCountryCodeAndLocale(
            UUID userId,
            LegalDocumentCode documentCode,
            String documentVersion,
            String countryCode,
            String locale);

    List<LegalAcceptance> findByUserIdOrderByAcceptedAtAsc(UUID userId);
}
