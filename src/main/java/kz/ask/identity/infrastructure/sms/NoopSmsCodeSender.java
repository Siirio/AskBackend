package kz.ask.identity.infrastructure.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "auth.verification.test-mode", havingValue = "false", matchIfMissing = true)
public class NoopSmsCodeSender implements SmsCodeSender {

    private static final Logger log = LoggerFactory.getLogger(NoopSmsCodeSender.class);

    @Override
    public void sendCode(String phone, String code) {
        log.info("SMS disabled. Code for {} would be: {}", phone, code);
    }
}
