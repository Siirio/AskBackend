package kz.ask.shared.error;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(ErrorCode errorCode, Object... args) {
        super(errorCode, HttpStatus.UNAUTHORIZED, args);
    }
}
