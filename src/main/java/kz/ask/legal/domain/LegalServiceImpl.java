package kz.ask.legal.domain;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import kz.ask.identity.infrastructure.repository.AppUserRepository;
import kz.ask.legal.domain.dto.LegalDocumentDto;
import kz.ask.legal.domain.entity.LegalAcceptance;
import kz.ask.legal.domain.entity.LegalDocument;
import kz.ask.legal.domain.enums.LegalAcceptanceChannel;
import kz.ask.legal.domain.enums.LegalDocumentCode;
import kz.ask.legal.infrastructure.repository.LegalAcceptanceRepository;
import kz.ask.legal.infrastructure.repository.LegalDocumentRepository;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LegalServiceImpl implements LegalService {

    private final LegalDocumentRepository legalDocumentRepository;
    private final LegalAcceptanceRepository legalAcceptanceRepository;
    private final AppUserRepository appUserRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LegalDocumentDto> activeDocuments(String countryCode, String locale) {
        return legalDocumentRepository
                .findByCountryCodeAndLocaleAndActiveTrueAndEffectiveAtLessThanEqualOrderByCode(
                        countryCode, locale, Instant.now())
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void acceptActiveDocuments(UUID userId,
                                      Collection<LegalDocumentCode> codes,
                                      String countryCode,
                                      String locale,
                                      LegalAcceptanceChannel channel) {
        for (LegalDocumentCode code : codes) {
            LegalDocument document = legalDocumentRepository
                    .findFirstByCodeAndCountryCodeAndLocaleAndActiveTrueAndEffectiveAtLessThanEqualOrderByEffectiveAtDesc(
                            code, countryCode, locale, Instant.now())
                    .orElseThrow(() -> new NotFoundException(ErrorCode.LEGAL_DOCUMENT_NOT_FOUND, code));
            if (legalAcceptanceRepository
                    .existsByUserIdAndDocumentCodeAndDocumentVersionAndCountryCodeAndLocale(
                            userId, code, document.getVersion(), countryCode, locale)) {
                continue;
            }
            LegalAcceptance acceptance = new LegalAcceptance();
            acceptance.setUser(appUserRepository.getReferenceById(userId));
            acceptance.setDocumentCode(code);
            acceptance.setDocumentVersion(document.getVersion());
            acceptance.setCountryCode(countryCode);
            acceptance.setLocale(locale);
            acceptance.setAcceptanceChannel(channel);
            acceptance.setAcceptedAt(Instant.now());
            legalAcceptanceRepository.save(acceptance);
        }
    }

    private LegalDocumentDto toDto(LegalDocument document) {
        return LegalDocumentDto.builder()
                .code(document.getCode())
                .version(document.getVersion())
                .countryCode(document.getCountryCode())
                .locale(document.getLocale())
                .publicUrl(document.getPublicUrl())
                .effectiveAt(document.getEffectiveAt())
                .build();
    }
}
