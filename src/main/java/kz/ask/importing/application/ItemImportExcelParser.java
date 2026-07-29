package kz.ask.importing.application;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.springframework.stereotype.Component;

@Component
public class ItemImportExcelParser {

    public ItemImportParsedWorkbook parse(InputStream inputStream) throws IOException {
        List<String> columns = new ArrayList<>();
        List<Map<String, String>> rows = new ArrayList<>();
        try (ReadableWorkbook workbook = new ReadableWorkbook(inputStream)) {
            List<Row> workbookRows = workbook.getFirstSheet().read();
            if (workbookRows.isEmpty()) {
                return ItemImportParsedWorkbook.builder().columns(columns).rows(rows).build();
            }
            Row header = workbookRows.get(0);
            for (int index = 0; index < header.getCellCount(); index++) {
                String column = header.getCellText(index).trim();
                if (!column.isEmpty()) columns.add(column);
            }
            for (int rowIndex = 1; rowIndex < workbookRows.size(); rowIndex++) {
                Row workbookRow = workbookRows.get(rowIndex);
                Map<String, String> values = new LinkedHashMap<>();
                boolean hasValue = false;
                for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
                    String value = columnIndex < workbookRow.getCellCount()
                            ? workbookRow.getCellText(columnIndex).trim()
                            : "";
                    values.put(columns.get(columnIndex), value);
                    hasValue = hasValue || !value.isEmpty();
                }
                if (hasValue) rows.add(values);
            }
        }
        return ItemImportParsedWorkbook.builder().columns(columns).rows(rows).build();
    }
}
