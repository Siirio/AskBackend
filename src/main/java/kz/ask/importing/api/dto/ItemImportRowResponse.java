package kz.ask.importing.api.dto;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemImportRowResponse {
    private UUID rowId;
    private Integer rowNumber;
    private String status;
    private Map<String, String> normalizedData;
    private List<String> errors;
    private List<String> warnings;
}
