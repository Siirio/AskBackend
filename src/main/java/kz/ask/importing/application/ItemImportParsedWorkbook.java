package kz.ask.importing.application;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemImportParsedWorkbook {
    private List<String> columns;
    private List<Map<String, String>> rows;
}
