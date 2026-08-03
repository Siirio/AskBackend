package kz.ask.legal.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;
import kz.ask.legal.domain.enums.LegalDocumentCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcceptLegalDocumentsRequest {

    @NotEmpty
    private Set<LegalDocumentCode> documentCodes;

    @NotBlank
    @Size(min = 2, max = 2)
    private String countryCode;

    @NotBlank
    @Size(max = 16)
    private String locale;
}
