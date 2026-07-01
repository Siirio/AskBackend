package kz.ask.shared.error;

import org.springframework.http.HttpStatus;

public class ExternalServiceException extends BusinessException {

    public ExternalServiceException(ErrorCode errorCode, Object... args) {
        super(errorCode, HttpStatus.BAD_GATEWAY, args);
    }
}
