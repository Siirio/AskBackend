package kz.ask.shared.error;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends BusinessException {

    public ForbiddenException(ErrorCode errorCode, Object... args) {
        super(errorCode, HttpStatus.FORBIDDEN, args);
    }
}
