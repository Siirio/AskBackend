package kz.ask.catalog.domain.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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

    public ExcelParseResult parse(InputStream inputStream, String originalFilename) throws IOException {
        if (originalFilename != null && originalFilename.toLowerCase().endsWith(".csv")) {
            return parseCsv(inputStream);
        }
        return parseExcel(inputStream);
    }

    public ExcelParseResult parse(InputStream inputStream) throws IOException {
        return parseExcel(inputStream);
    }

    private ExcelParseResult parseExcel(InputStream inputStream) throws IOException {
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

    private ExcelParseResult parseCsv(InputStream inputStream) throws IOException {
        String text = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        String normalized = text.replace("\r\n", "\n").replace('\r', '\n');
        List<String> lines = normalized.lines()
            .filter(line -> !line.isBlank())
            .toList();
        List<String> columns = new ArrayList<>();
        List<Map<String, String>> rows = new ArrayList<>();

        if (lines.isEmpty()) {
            return new ExcelParseResult(columns, rows);
        }

        char delimiter = detectDelimiter(lines.get(0));
        for (String column : parseCsvLine(lines.get(0), delimiter)) {
            String clean = column.trim();
            if (!clean.isEmpty()) {
                columns.add(clean);
            }
        }

        for (int i = 1; i < lines.size(); i++) {
            List<String> values = parseCsvLine(lines.get(i), delimiter);
            Map<String, String> rowData = new LinkedHashMap<>();
            Boolean hasValue = false;
            for (int c = 0; c < columns.size(); c++) {
                String value = c < values.size() ? values.get(c).trim() : "";
                rowData.put(columns.get(c), value);
                if (!value.isEmpty()) {
                    hasValue = true;
                }
            }
            if (hasValue) {
                rows.add(rowData);
            }
        }

        return new ExcelParseResult(columns, rows);
    }

    private char detectDelimiter(String line) {
        Integer commaCount = count(line, ',');
        Integer semicolonCount = count(line, ';');
        Integer tabCount = count(line, '\t');
        if (tabCount >= commaCount && tabCount >= semicolonCount) {
            return '\t';
        }
        return semicolonCount > commaCount ? ';' : ',';
    }

    private Integer count(String line, char target) {
        Integer result = 0;
        Boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char current = line.charAt(i);
            if (current == '"') {
                quoted = !quoted;
            } else if (!quoted && current == target) {
                result++;
            }
        }
        return result;
    }

    private List<String> parseCsvLine(String line, char delimiter) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        Boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (!quoted && ch == delimiter) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        values.add(current.toString());
        return values;
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
