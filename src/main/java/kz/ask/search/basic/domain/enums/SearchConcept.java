package kz.ask.search.basic.domain.enums;

import java.util.Arrays;
import java.util.Optional;

public enum SearchConcept {
    ACTIVE_LEISURE,
    RIDE_ACTIVITY,
    MOTORSPORT,
    GAMING,
    BEAUTY,
    CLEANING,
    REPAIR,
    EDUCATION,
    HEALTHCARE,
    GROUP_ACTIVITY,
    KIDS_ACTIVITY,
    DATE_ACTIVITY,
    INDOOR_ENTERTAINMENT;

    public static Optional<SearchConcept> resolve(String value) {
        if (value == null) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(concept -> concept.name().equalsIgnoreCase(value.trim()))
                .findFirst();
    }
}
