package kz.ask.catalog.domain.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RawCatalogRowDto {

    private UUID id;
    private Integer rowNumber;
    private String rowPayload;
    private String normalizedDataJson;
    private String validationErrorsJson;
    private String validationWarningsJson;
    private String status;
}
