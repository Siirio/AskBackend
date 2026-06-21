package kz.ask.identity.application;

public class AuthRoleMismatchException extends RuntimeException {

    public AuthRoleMismatchException(String msg) {
        super(msg);
    }
}
