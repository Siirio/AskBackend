package kz.ask.identity.infrastructure.mail;

import kz.ask.identity.domain.VerificationDeliveryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "auth.verification.email.enabled", havingValue = "true", matchIfMissing = true)
public class SmtpEmailCodeSender implements EmailCodeSender {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailCodeSender.class);

    private final JavaMailSender mailSender;
    private final String from;

    public SmtpEmailCodeSender(JavaMailSender mailSender,
                               @Value("${auth.email.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void sendCode(String email, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(email);
            message.setSubject("Ask verification code");
            message.setText("Your Ask verification code: " + code);
            mailSender.send(message);
            log.info("Verification code sent to {}", email);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", email, e.getMessage());
            throw new VerificationDeliveryException("Email verification code was not sent.");
        }
    }
}
