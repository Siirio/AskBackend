package kz.ask.legal.domain;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;
import kz.ask.identity.infrastructure.repository.AppUserRepository;
import kz.ask.legal.domain.entity.LegalAcceptance;
import kz.ask.legal.domain.enums.LegalAcceptanceChannel;
import kz.ask.legal.domain.enums.LegalDocumentCode;
import kz.ask.legal.infrastructure.repository.LegalAcceptanceRepository;
import kz.ask.legal.infrastructure.repository.LegalDocumentRepository;
import kz.ask.legal.domain.entity.LegalDocument;
import kz.ask.legal.domain.dto.LegalDocumentDto;
import java.util.List;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LegalServiceImpl implements LegalService {

    private final LegalAcceptanceRepository legalAcceptanceRepository;
    private final AppUserRepository appUserRepository;
    private final LegalDocumentRepository legalDocumentRepository;

    @Override
    @Transactional
    public void acceptActiveDocuments(UUID userId,
                                      Collection<LegalDocumentCode> codes,
                                      String countryCode,
                                      String locale,
                                      LegalAcceptanceChannel channel) {
        List<LegalDocument> documents = legalDocumentRepository
                .findByCountryCodeAndCodeInAndIsActiveTrue(countryCode, List.copyOf(codes));
        if (documents.size() != codes.stream().distinct().count()) {
            throw new ValidationException(ErrorCode.LEGAL_DOCUMENT_NOT_FOUND);
        }
        for (LegalDocument document : documents) {
            if (legalAcceptanceRepository
                    .existsByUserIdAndDocumentCodeAndDocumentVersionAndCountryCodeAndLocale(
                            userId, document.getCode(), document.getVersion(), countryCode, locale)) {
                continue;
            }
            LegalAcceptance acceptance = new LegalAcceptance();
            acceptance.setUser(appUserRepository.getReferenceById(userId));
            acceptance.setDocumentCode(document.getCode());
            acceptance.setDocumentVersion(document.getVersion());
            acceptance.setCountryCode(countryCode);
            acceptance.setLocale(locale);
            acceptance.setAcceptanceChannel(channel);
            acceptance.setAcceptedAt(Instant.now());
            legalAcceptanceRepository.save(acceptance);
        }
    }

    @Override
    public List<LegalDocumentDto> listActiveDocuments(String countryCode) {
        return legalDocumentRepository.findByCountryCodeAndIsActiveTrueOrderByCodeAsc(countryCode)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LegalDocumentCode> pendingDocuments(UUID userId, String countryCode) {
        if (legalAcceptanceRepository.existsByUserId(userId)) {
            return List.of();
        }
        List<LegalDocumentCode> baseline = List.of(
                LegalDocumentCode.USER_TERMS,
                LegalDocumentCode.PRIVACY_POLICY);
        return legalDocumentRepository.findByCountryCodeAndCodeInAndIsActiveTrue(countryCode, baseline)
                .stream()
                .map(LegalDocument::getCode)
                .toList();
    }

    private LegalDocumentDto toDto(LegalDocument document) {
        return LegalDocumentDto.builder()
                .code(document.getCode())
                .version(document.getVersion())
                .countryCode(document.getCountryCode())
                .publicUrl(document.getPublicUrl())
                .effectiveAt(document.getEffectiveAt())
                .build();
    }
}
