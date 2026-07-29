package kz.ask.legal.domain;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;
import kz.ask.identity.infrastructure.repository.AppUserRepository;
import kz.ask.legal.domain.entity.LegalAcceptance;
import kz.ask.legal.domain.enums.LegalAcceptanceChannel;
import kz.ask.legal.domain.enums.LegalDocumentCode;
import kz.ask.legal.infrastructure.repository.LegalAcceptanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LegalServiceImpl implements LegalService {

    private final LegalAcceptanceRepository legalAcceptanceRepository;
    private final AppUserRepository appUserRepository;

    @Override
    @Transactional
    public void acceptActiveDocuments(UUID userId,
                                      Collection<LegalDocumentCode> codes,
                                      String countryCode,
                                      String locale,
                                      LegalAcceptanceChannel channel) {
        for (LegalDocumentCode code : codes) {
            if (legalAcceptanceRepository
                    .existsByUserIdAndDocumentCodeAndCountryCodeAndLocale(
                            userId, code, countryCode, locale)) {
                continue;
            }
            LegalAcceptance acceptance = new LegalAcceptance();
            acceptance.setUser(appUserRepository.getReferenceById(userId));
            acceptance.setDocumentCode(code);
            acceptance.setCountryCode(countryCode);
            acceptance.setLocale(locale);
            acceptance.setAcceptanceChannel(channel);
            acceptance.setAcceptedAt(Instant.now());
            legalAcceptanceRepository.save(acceptance);
        }
    }

    @Override
    public Boolean hasAcceptedAnyDocuments(UUID userId, Collection<LegalDocumentCode> codes) {
        return legalAcceptanceRepository.findByUserIdOrderByAcceptedAtAsc(userId).stream()
                .anyMatch(acceptance -> codes.contains(acceptance.getDocumentCode()));
    }
}
