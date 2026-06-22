package kz.ask.catalog.api.dto;

import java.util.List;
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
public class PreviewResponse {
    private UUID importId;
    private String status;
    private Integer totalRows;
    private Integer validRows;
    private Integer invalidRows;
    private Integer warningRows;
    private List<ColumnMappingInfo> mappings;
    private List<RowPreview> rows;
}
