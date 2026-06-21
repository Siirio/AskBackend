package kz.ask.shared.error;

import org.springframework.http.HttpStatus;

public class NotFoundException extends BusinessException {

    public NotFoundException(ErrorCode errorCode, Object... args) {
        super(errorCode, HttpStatus.NOT_FOUND, args);
    }
}
