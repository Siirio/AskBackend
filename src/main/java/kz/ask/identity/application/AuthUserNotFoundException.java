package kz.ask.identity.application;

public class AuthUserNotFoundException extends RuntimeException {

    public AuthUserNotFoundException(String msg) {
        super(msg);
    }
}
