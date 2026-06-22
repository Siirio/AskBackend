package kz.ask.catalog.domain.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CatalogImportDto {

    private UUID id;
    private String originalFileName;
    private String status;
    private Integer totalRows;
    private Integer validRows;
    private Integer invalidRows;
    private Integer warningRows;
    private Instant importedAt;
}
