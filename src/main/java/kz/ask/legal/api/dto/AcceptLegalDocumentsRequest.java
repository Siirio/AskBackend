package kz.ask.legal.api.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
import kz.ask.legal.domain.enums.LegalDocumentCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcceptLegalDocumentsRequest {

    @NotEmpty
    private Set<LegalDocumentCode> documentCodes;

    private String countryCode = "KZ";

    private String locale = "ru";
}
