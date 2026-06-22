package kz.ask.catalog.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class MappingEntry {
    @NotBlank
    private String sourceColumn;
    @NotNull
    private TargetField targetField;
    private String characteristicName;
}
