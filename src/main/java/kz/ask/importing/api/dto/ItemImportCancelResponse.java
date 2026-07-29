package kz.ask.importing.api.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemImportCancelResponse {
    private UUID importId;
    private String status;
}
