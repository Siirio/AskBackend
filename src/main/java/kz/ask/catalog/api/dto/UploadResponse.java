package kz.ask.catalog.api.dto;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponse {
    private UUID importId;
    private String originalFileName;
    private String status;
    private Integer totalRows;
    private List<ColumnInfo> columns;
    private List<Map<String, String>> sampleRows;
}
