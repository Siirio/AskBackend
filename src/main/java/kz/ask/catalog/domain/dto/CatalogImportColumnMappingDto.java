package kz.ask.catalog.domain.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CatalogImportColumnMappingDto {

    private UUID id;
    private String sourceColumn;
    private String targetField;
    private String characteristicName;
    private Boolean approved;
    private Double confidence;
}
