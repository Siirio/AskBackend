package kz.ask.business.invitation.infrastructure.mail;

public interface BusinessInvitationEmailSender {

    void sendInvitation(String email, String rawToken);
}
