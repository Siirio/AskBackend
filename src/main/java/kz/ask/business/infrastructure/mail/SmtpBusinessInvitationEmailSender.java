package kz.ask.business.infrastructure.mail;

import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ExternalServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "auth.verification.test-mode", havingValue = "false", matchIfMissing = true)
public class SmtpBusinessInvitationEmailSender implements BusinessInvitationEmailSender {

    private final JavaMailSender mailSender;
    private final String from;
    private final String frontendBaseUrl;

    public SmtpBusinessInvitationEmailSender(
            JavaMailSender mailSender,
            @Value("${auth.email.from}") String from,
            @Value("${ask.frontend.base-url}") String frontendBaseUrl) {
        this.mailSender = mailSender;
        this.from = from;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    @Override
    public void sendInvitation(String email, String rawToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(email);
            message.setSubject("Ask business invitation");
            message.setText("Open your Ask invitation: "
                    + frontendBaseUrl
                    + "/auth?invitation="
                    + rawToken);
            mailSender.send(message);
        } catch (Exception exception) {
            throw new ExternalServiceException(ErrorCode.DELIVERY_FAILED);
        }
    }
}
