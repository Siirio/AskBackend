package kz.ask.legal.api.dto;

import java.time.Instant;
import kz.ask.legal.domain.enums.LegalDocumentCode;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LegalDocumentResponse {
    private LegalDocumentCode code;
    private String version;
    private String countryCode;
    private String locale;
    private String publicUrl;
    private Instant effectiveAt;
}
