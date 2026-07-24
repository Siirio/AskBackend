package kz.ask.importing.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemImportMappingRequest {
    @Valid
    @NotEmpty
    private List<ItemImportMappingEntry> mappings;
}
