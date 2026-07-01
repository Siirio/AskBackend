package kz.ask.catalog.domain.service;

import java.util.List;
import java.util.Map;

public record ExcelParseResult(List<String> columns, List<Map<String, String>> rows) {}
