package kz.ask.legal.domain;

import java.util.Collection;
import java.util.UUID;
import kz.ask.legal.domain.enums.LegalAcceptanceChannel;
import kz.ask.legal.domain.enums.LegalDocumentCode;
import kz.ask.legal.domain.dto.LegalDocumentDto;
import java.util.List;

public interface LegalService {

    void acceptActiveDocuments(UUID userId,
                               Collection<LegalDocumentCode> codes,
                               String countryCode,
                               String locale,
                               LegalAcceptanceChannel channel);

    List<LegalDocumentDto> listActiveDocuments(String countryCode);

    List<LegalDocumentCode> pendingDocuments(UUID userId, String countryCode);
}
