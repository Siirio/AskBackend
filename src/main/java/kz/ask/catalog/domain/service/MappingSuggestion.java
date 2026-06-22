package kz.ask.catalog.domain.service;

import kz.ask.catalog.domain.enums.TargetField;

public record MappingSuggestion(TargetField targetField, Double confidence) {}
