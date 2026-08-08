package kz.ask.search.basic.domain;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class SearchTextNormalizer {

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern NON_WORD = Pattern.compile("[^\\p{L}\\p{N}\\s]");

    private SearchTextNormalizer() {
    }

    public static String normalize(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String result = text.toLowerCase();
        result = Normalizer.normalize(result, Normalizer.Form.NFC);
        result = result.replace('ё', 'е');
        result = NON_WORD.matcher(result).replaceAll(" ");
        result = WHITESPACE.matcher(result).replaceAll(" ").trim();
        return result;
    }

    public static List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String normalized = normalize(text);
        if (normalized.isEmpty()) {
            return List.of();
        }
        List<String> tokens = new ArrayList<>();
        for (String token : normalized.split(" ")) {
            String trimmed = token.trim();
            if (!trimmed.isEmpty() && trimmed.length() >= 2) {
                tokens.add(trimmed);
            }
        }
        return tokens;
    }

    public static List<String> tokenize(String name, String description, String category,
                                        List<String> tags, String businessName) {
        return tokenize(name, description, category, tags, businessName, "");
    }

    public static List<String> tokenize(String name, String description, String category,
                                        List<String> tags, String businessName, String attributeText) {
        List<String> tokens = new ArrayList<>();
        tokens.addAll(tokenize(name));
        tokens.addAll(tokenize(description));
        tokens.addAll(tokenize(category));
        if (tags != null) {
            for (String tag : tags) {
                tokens.addAll(tokenize(tag));
            }
        }
        tokens.addAll(tokenize(businessName));
        tokens.addAll(tokenize(attributeText));
        return tokens.stream().distinct().toList();
    }
}
