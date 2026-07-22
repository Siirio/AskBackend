package kz.ask.business.invitation.infrastructure.mail;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "auth.verification.test-mode", havingValue = "true")
public class LocalBusinessInvitationEmailSender implements BusinessInvitationEmailSender {

    @Override
    public void sendInvitation(String email, String rawToken) {
    }
}
