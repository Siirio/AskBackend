package kz.ask.managedimport.infrastructure.scheduler;

import java.time.Instant;
import kz.ask.managedimport.domain.ManagedImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ManagedImportExpirationScheduler {

    private final ManagedImportService managedImportService;

    @Scheduled(fixedDelayString = "${business.managed-import.expiration-check-interval:PT5M}")
    public void expireDue() {
        managedImportService.expireDue(Instant.now());
    }
}
