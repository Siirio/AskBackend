package kz.ask.offer.media;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ValidationException;

public final class CatalogImageLayout {

    public static final int MAX_IMAGES = 3;
    private static final String NEW_PREFIX = "new:";

    private CatalogImageLayout() {
    }

    public static List<String> resolve(
            List<String> currentFiles,
            List<String> newFiles,
            List<String> order) {
        if (order.size() > MAX_IMAGES) {
            throw new ValidationException(ErrorCode.CATALOG_IMAGE_LIMIT_EXCEEDED);
        }
        Set<String> current = new HashSet<>(currentFiles);
        Set<String> used = new HashSet<>();
        List<String> resolved = new ArrayList<>();
        for (String token : order) {
            String storedName = resolveToken(token, current, newFiles);
            if (!used.add(storedName)) {
                throw new ValidationException(ErrorCode.CATALOG_IMAGE_ORDER_INVALID);
            }
            resolved.add(storedName);
        }
        if (newFiles.stream().anyMatch(file -> !used.contains(file))) {
            throw new ValidationException(ErrorCode.CATALOG_IMAGE_ORDER_INVALID);
        }
        return List.copyOf(resolved);
    }

    private static String resolveToken(String token, Set<String> current, List<String> newFiles) {
        if (token != null && token.startsWith(NEW_PREFIX)) {
            try {
                int index = Integer.parseInt(token.substring(NEW_PREFIX.length()));
                if (index >= 0 && index < newFiles.size()) {
                    return newFiles.get(index);
                }
            } catch (NumberFormatException ignored) {
                throw new ValidationException(ErrorCode.CATALOG_IMAGE_ORDER_INVALID);
            }
        } else if (current.contains(token)) {
            return token;
        }
        throw new ValidationException(ErrorCode.CATALOG_IMAGE_ORDER_INVALID);
    }
}
