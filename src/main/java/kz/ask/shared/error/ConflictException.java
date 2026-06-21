package kz.ask.shared.error;

import org.springframework.http.HttpStatus;

public class ConflictException extends BusinessException {

    public ConflictException(ErrorCode errorCode, Object... args) {
        super(errorCode, HttpStatus.CONFLICT, args);
    }
}
