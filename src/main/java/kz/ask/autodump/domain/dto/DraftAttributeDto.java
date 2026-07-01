package kz.ask.autodump.domain.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DraftAttributeDto {

    private UUID id;
    private UUID draftItemId;
    private String attributeKey;
    private String attributeValue;
    private String source;
}
