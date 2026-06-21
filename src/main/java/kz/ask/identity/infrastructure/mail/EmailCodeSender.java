package kz.ask.identity.infrastructure.mail;

public interface EmailCodeSender {
    void sendCode(String email, String code);
}
