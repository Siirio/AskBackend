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
public class RowPreview {
    private UUID rowId;
    private Integer rowNumber;
    private String status;
    private Map<String, String> normalizedData;
    private List<String> errors;
    private List<String> warnings;
}
