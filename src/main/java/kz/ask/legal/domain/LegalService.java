package kz.ask.legal.domain;

import java.util.Collection;
import java.util.UUID;
import kz.ask.legal.domain.enums.LegalAcceptanceChannel;
import kz.ask.legal.domain.enums.LegalDocumentCode;

public interface LegalService {

    void acceptActiveDocuments(UUID userId,
                               Collection<LegalDocumentCode> codes,
                               String countryCode,
                               String locale,
                               LegalAcceptanceChannel channel);

    Boolean hasAcceptedAnyDocuments(UUID userId, Collection<LegalDocumentCode> codes);
}
