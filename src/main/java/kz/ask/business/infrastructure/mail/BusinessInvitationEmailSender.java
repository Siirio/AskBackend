package kz.ask.business.infrastructure.mail;

public interface BusinessInvitationEmailSender {

    void sendInvitation(String email, String rawToken);
}
