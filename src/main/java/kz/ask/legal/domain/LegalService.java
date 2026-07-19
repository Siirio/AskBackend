package kz.ask.legal.domain;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import kz.ask.legal.domain.dto.LegalDocumentDto;
import kz.ask.legal.domain.enums.LegalAcceptanceChannel;
import kz.ask.legal.domain.enums.LegalDocumentCode;

public interface LegalService {

    List<LegalDocumentDto> activeDocuments(String countryCode, String locale);

    void acceptActiveDocuments(UUID userId,
                               Collection<LegalDocumentCode> codes,
                               String countryCode,
                               String locale,
                               LegalAcceptanceChannel channel);

    Boolean hasAcceptedActiveDocuments(UUID userId,
                                       Collection<LegalDocumentCode> codes,
                                       String countryCode,
                                       String locale);

    Boolean hasAcceptedAnyDocuments(UUID userId, Collection<LegalDocumentCode> codes);
}
