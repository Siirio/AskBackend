package kz.ask.search.basic.domain;

import java.util.List;
import java.util.Set;

public final class AttributeKeys {

    public static final String COLOR = "color";
    public static final String SIZE = "size";
    public static final String BRAND = "brand";
    public static final String MATERIAL = "material";
    public static final String AUDIENCE = "audience";
    public static final String OCCASION = "occasion";
    public static final String CONDITION = "condition";

    public static final Set<String> ARRAY_KEYS = Set.of(COLOR, AUDIENCE, OCCASION);
    public static final Set<String> SINGLE_VALUE_KEYS = Set.of(SIZE, BRAND, MATERIAL, CONDITION);
    public static final Set<String> ALL_KEYS = Set.of(COLOR, SIZE, BRAND, MATERIAL, AUDIENCE, OCCASION, CONDITION);

    public static final List<String> AUDIENCE_VALUES = List.of("women", "men", "unisex", "kids");
    public static final List<String> OCCASION_VALUES = List.of("birthday", "gift", "everyday");
    public static final List<String> CONDITION_VALUES = List.of("new", "used");

    private AttributeKeys() {
    }
}
