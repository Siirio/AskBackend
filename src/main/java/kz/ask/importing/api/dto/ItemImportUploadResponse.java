package kz.ask.importing.api.dto;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemImportUploadResponse {
    private UUID importId;
    private String originalFileName;
    private String status;
    private Integer totalRows;
    private List<ItemImportColumnInfo> columns;
    private List<Map<String, String>> sampleRows;
}
