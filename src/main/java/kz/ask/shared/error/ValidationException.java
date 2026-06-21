package kz.ask.shared.error;

import org.springframework.http.HttpStatus;

public class ValidationException extends BusinessException {

    public ValidationException(ErrorCode errorCode, Object... args) {
        super(errorCode, HttpStatus.BAD_REQUEST, args);
    }
}
