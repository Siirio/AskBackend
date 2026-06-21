package kz.ask.shared.error;

import org.springframework.http.HttpStatus;

public class InternalServerException extends BusinessException {

    public InternalServerException(ErrorCode errorCode, Object... args) {
        super(errorCode, HttpStatus.INTERNAL_SERVER_ERROR, args);
    }
}
