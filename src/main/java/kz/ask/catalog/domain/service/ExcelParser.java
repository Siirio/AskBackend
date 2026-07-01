package kz.ask.catalog.domain.service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.dhatim.fastexcel.reader.Cell;
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
                String col = cellAsString(headerRow, i).trim();
                if (!col.isEmpty()) {
                    columns.add(col);
                }
            }

            for (int r = 1; r < allRows.size(); r++) {
                Row row = allRows.get(r);
                Map<String, String> rowData = new LinkedHashMap<>();
                boolean hasValue = false;
                for (int i = 0; i < columns.size() && i < row.getCellCount(); i++) {
                    String value = cellAsString(row, i).trim();
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

    private String cellAsString(Row row, int index) {
        if (index >= row.getCellCount()) {
            return "";
        }
        Cell cell = row.getCell(index);
        if (cell == null) {
            return "";
        }
        return switch (cell.getType()) {
            case STRING -> cell.asString();
            case NUMBER -> {
                BigDecimal d = cell.asNumber();
                yield d.stripTrailingZeros().scale() <= 0
                    ? d.toBigInteger().toString()
                    : d.toPlainString();
            }
            case BOOLEAN -> String.valueOf(cell.asBoolean());
            case EMPTY -> "";
            case ERROR -> "";
            case FORMULA -> cell.getText();
        };
    }
}
