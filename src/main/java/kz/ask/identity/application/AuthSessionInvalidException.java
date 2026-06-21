package kz.ask.identity.application;

public class AuthSessionInvalidException extends RuntimeException {

    public AuthSessionInvalidException(String msg) {
        super(msg);
    }
}
