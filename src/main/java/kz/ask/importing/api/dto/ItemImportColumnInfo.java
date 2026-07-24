package kz.ask.importing.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemImportColumnInfo {
    private String sourceColumn;
    private ItemImportTargetField suggestedTargetField;
    private Double confidence;
}
