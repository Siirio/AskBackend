package kz.ask.importing.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ItemImportMappingEntry {
    @NotBlank
    private String sourceColumn;
    @NotNull
    private ItemImportTargetField targetField;
    private String characteristicName;
}
