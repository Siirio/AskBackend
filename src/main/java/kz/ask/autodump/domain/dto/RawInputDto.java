package kz.ask.autodump.domain.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RawInputDto {

    private UUID id;
    private UUID importSessionId;
    private String originalFileName;
    private String contentType;
    private String storageKind;
    private String storageRef;
    private String rawText;
    private String sha256;
    private Long sizeBytes;
}
