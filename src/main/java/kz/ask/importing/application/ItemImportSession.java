package kz.ask.importing.application;

import java.util.List;
import java.util.UUID;
import kz.ask.business.category.domain.enums.CategoryType;
import kz.ask.importing.api.dto.ItemImportMappingEntry;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ItemImportSession {
    private UUID id;
    private UUID businessId;
    private UUID branchId;
    private UUID createdByUserId;
    private CategoryType type;
    private String originalFileName;
    private String status;
    private List<String> columns;
    private List<ItemImportMappingEntry> mappings;
    private List<ItemImportRow> rows;
}
