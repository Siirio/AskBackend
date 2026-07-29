package kz.ask.identity.infrastructure.scheduler;

import java.time.Duration;
import java.time.Instant;
import kz.ask.identity.infrastructure.repository.VerificationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class VerificationRetentionScheduler {

    private final VerificationRepository verificationRepository;
    private final Duration retention;

    public VerificationRetentionScheduler(
            VerificationRepository verificationRepository,
            @Value("${auth.challenge.retention:P1D}") Duration retention) {
        this.verificationRepository = verificationRepository;
        this.retention = retention;
    }

    @Scheduled(fixedDelayString = "${auth.challenge.retention-interval:PT1H}")
    @Transactional
    public void deleteExpiredChallenges() {
        verificationRepository.deleteByExpiresAtBefore(Instant.now().minus(retention));
    }
}
