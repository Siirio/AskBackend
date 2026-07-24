package kz.ask.importing.api.dto;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemImportPreviewResponse {
    private UUID importId;
    private String status;
    private Integer totalRows;
    private Integer validRows;
    private Integer invalidRows;
    private Integer warningRows;
    private List<ItemImportMappingEntry> mappings;
    private List<ItemImportRowResponse> rows;
}
