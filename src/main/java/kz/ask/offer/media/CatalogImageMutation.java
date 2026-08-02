package kz.ask.offer.media;

import java.util.ArrayList;
import java.util.List;
import kz.ask.business.media.application.ManagedImageStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class CatalogImageMutation {

    private final ManagedImageStorage imageStorage;

    public Result apply(List<String> currentFiles, List<MultipartFile> files, List<String> order) {
        List<String> added = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                added.add(imageStorage.store(file));
            }
            List<String> ordered = CatalogImageLayout.resolve(currentFiles, added, order);
            List<String> removed = currentFiles.stream().filter(file -> !ordered.contains(file)).toList();
            registerCleanup(added, removed);
            return new Result(ordered);
        } catch (RuntimeException exception) {
            added.forEach(imageStorage::delete);
            throw exception;
        }
    }

    public List<CatalogImageResponse> toResponses(List<String> storedNames) {
        return (storedNames == null ? List.<String>of() : storedNames).stream()
                .map(storedName -> CatalogImageResponse.builder()
                        .id(storedName)
                        .url(imageStorage.publicUrl(storedName))
                        .build())
                .toList();
    }

    public void deleteAfterCommit(List<String> storedNames) {
        registerCleanup(List.of(), List.copyOf(storedNames));
    }

    private void registerCleanup(List<String> added, List<String> removed) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("Catalog image mutations require an active transaction");
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                List<String> cleanup = status == TransactionSynchronization.STATUS_COMMITTED ? removed : added;
                cleanup.forEach(imageStorage::delete);
            }
        });
    }

    public record Result(List<String> storedNames) {
    }
}
