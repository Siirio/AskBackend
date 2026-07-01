package kz.ask.autodump.domain.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ImportErrorDto {

    private UUID id;
    private UUID importSessionId;
    private UUID draftItemId;
    private String severity;
    private String code;
    private String message;
    private String payloadJson;
}
