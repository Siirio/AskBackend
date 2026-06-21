package kz.ask.shared.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final HttpStatus httpStatus;
    private final Object[] args;

    protected BusinessException(ErrorCode errorCode, HttpStatus httpStatus, Object... args) {
        super(errorCode.format(args));
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.args = args;
    }
}
