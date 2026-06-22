package kz.ask.catalog.api.dto;

import kz.ask.catalog.domain.enums.TargetField;
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
public class ColumnMappingInfo {
    private String sourceColumn;
    private TargetField targetField;
    private String characteristicName;
    private Boolean approved;
    private Double confidence;
}
