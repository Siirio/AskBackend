package kz.ask.identity.infrastructure.scheduler;

import java.time.Duration;
import java.time.Instant;
import kz.ask.identity.infrastructure.repository.AuthChallengeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AuthChallengeRetentionScheduler {

    private final AuthChallengeRepository authChallengeRepository;
    private final Duration retention;

    public AuthChallengeRetentionScheduler(
            AuthChallengeRepository authChallengeRepository,
            @Value("${auth.challenge.retention:P1D}") Duration retention) {
        this.authChallengeRepository = authChallengeRepository;
        this.retention = retention;
    }

    @Scheduled(fixedDelayString = "${auth.challenge.retention-interval:PT1H}")
    @Transactional
    public void deleteExpiredChallenges() {
        authChallengeRepository.deleteByExpiresAtBefore(Instant.now().minus(retention));
    }
}
