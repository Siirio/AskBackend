package kz.ask.identity.infrastructure.sms;

import kz.ask.identity.domain.VerificationDeliveryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "auth.verification.sms.enabled", havingValue = "true", matchIfMissing = true)
public class LoggingSmsCodeSender implements SmsCodeSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingSmsCodeSender.class);

    @Override
    public void sendCode(String phone, String code) {
        log.info("SMS verification code for {}: {}", phone, code);
        throw new VerificationDeliveryException("SMS verification is not connected yet.");
    }
}
