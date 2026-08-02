package kz.ask.offer.media;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import kz.ask.shared.error.ValidationException;
import org.junit.jupiter.api.Test;

class CatalogImageLayoutTest {

    @Test
    void resolvesExistingAndNewFilesInRequestedOrder() {
        List<String> resolved = CatalogImageLayout.resolve(
                List.of("first.webp", "second.webp"),
                List.of("new-a.webp"),
                List.of("second.webp", "new:0", "first.webp"));

        assertEquals(List.of("second.webp", "new-a.webp", "first.webp"), resolved);
    }

    @Test
    void rejectsMoreThanThreeImages() {
        assertThrows(ValidationException.class, () -> CatalogImageLayout.resolve(
                List.of(),
                List.of("one.webp", "two.webp", "three.webp", "four.webp"),
                List.of("new:0", "new:1", "new:2", "new:3")));
    }

    @Test
    void rejectsUnknownExistingImage() {
        assertThrows(ValidationException.class, () -> CatalogImageLayout.resolve(
                List.of("known.webp"),
                List.of(),
                List.of("unknown.webp")));
    }
}
