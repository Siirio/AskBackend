package kz.ask.importing.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ItemImportRow {
    private UUID id;
    private Integer rowNumber;
    private Map<String, String> source;
    private Map<String, String> normalized;
    private String status;
    private List<String> errors;
    private List<String> warnings;
}
