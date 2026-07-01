package kz.ask.identity.infrastructure.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "auth.verification.email.enabled", havingValue = "false")
public class LoggingEmailCodeSender implements EmailCodeSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailCodeSender.class);

    @Override
    public void sendCode(String email, String code) {
        log.info("EMAIL verification code for {}: {}", email, code);
    }
}
