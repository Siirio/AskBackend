package kz.ask.catalog.domain.service;

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
public class ExcelParser {

    public ExcelParseResult parse(InputStream inputStream) throws IOException {
        List<String> columns = new ArrayList<>();
        List<Map<String, String>> rows = new ArrayList<>();

        try (ReadableWorkbook wb = new ReadableWorkbook(inputStream)) {
            var sheet = wb.getFirstSheet();
            List<Row> allRows = sheet.read();

            if (allRows.isEmpty()) {
                return new ExcelParseResult(columns, rows);
            }

            Row headerRow = allRows.get(0);
            for (int i = 0; i < headerRow.getCellCount(); i++) {
                String col = headerRow.getCellAsString(i).orElse("").trim();
                if (!col.isEmpty()) {
                    columns.add(col);
                }
            }

            for (int r = 1; r < allRows.size(); r++) {
                Row row = allRows.get(r);
                Map<String, String> rowData = new LinkedHashMap<>();
                boolean hasValue = false;
                for (int i = 0; i < columns.size() && i < row.getCellCount(); i++) {
                    String value = row.getCellAsString(i).orElse("").trim();
                    rowData.put(columns.get(i), value);
                    if (!value.isEmpty()) {
                        hasValue = true;
                    }
                }
                if (hasValue) {
                    rows.add(rowData);
                }
            }
        }

        return new ExcelParseResult(columns, rows);
    }
}
