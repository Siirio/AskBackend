package kz.ask.catalog.api.dto;

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
public class ColumnInfo {
    private String sourceColumn;
    private String suggestedTargetField;
    private Double confidence;
}
