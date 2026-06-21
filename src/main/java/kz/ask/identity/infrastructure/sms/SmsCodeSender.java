package kz.ask.identity.infrastructure.sms;

public interface SmsCodeSender {
    void sendCode(String phone, String code);
}
