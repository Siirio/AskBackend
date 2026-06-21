package kz.ask.shared.error;

import org.springframework.http.HttpStatus;

public class AuthException extends BusinessException {

    public AuthException(ErrorCode errorCode, Object... args) {
        super(errorCode, HttpStatus.UNAUTHORIZED, args);
    }
}
