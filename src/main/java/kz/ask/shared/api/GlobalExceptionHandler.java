package kz.ask.shared.api;

import java.time.Instant;
import kz.ask.shared.api.dto.ErrorDetail;
import kz.ask.shared.api.dto.ErrorResponse;
import kz.ask.shared.error.AuthException;
import kz.ask.shared.error.BusinessException;
import kz.ask.shared.error.ConflictException;
import kz.ask.shared.error.ExternalServiceException;
import kz.ask.shared.error.ForbiddenException;
import kz.ask.shared.error.InternalServerException;
import kz.ask.shared.error.NotFoundException;
import kz.ask.shared.error.UnauthorizedException;
import kz.ask.shared.error.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice(basePackages = "kz.ask")
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(toResponse(ex));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(toResponse(ex));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(toResponse(ex));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(toResponse(ex));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(toResponse(ex));
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuth(AuthException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(toResponse(ex));
    }

    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<ErrorResponse> handleInternal(InternalServerException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(toResponse(ex));
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ErrorResponse> handleExternal(ExternalServiceException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(toResponse(ex));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ErrorResponse.builder()
                        .timestamp(Instant.now())
                        .errorCode("FILE_TOO_LARGE")
                        .message("Размер файла превышает допустимый лимит")
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .timestamp(Instant.now())
                        .errorCode("VALIDATION_ERROR")
                        .message("Проверьте данные и попробуйте снова")
                        .errors(ex.getBindingResult().getFieldErrors().stream()
                                .map(this::toDetail)
                                .toList())
                        .build());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .timestamp(Instant.now())
                        .errorCode("MALFORMED_REQUEST")
                        .message("Некорректный формат запроса")
                        .build());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .timestamp(Instant.now())
                        .errorCode("MALFORMED_REQUEST")
                        .message("Некорректный формат запроса")
                        .build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.builder()
                        .timestamp(Instant.now())
                        .errorCode("ACCESS_DENIED")
                        .message("Доступ запрещен")
                        .build());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation", ex);
        Throwable cause = ex.getMostSpecificCause();
        String detail = cause != null ? cause.getMessage() : ex.getMessage();
        if (detail != null && detail.toLowerCase().contains("value too long")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.builder()
                            .timestamp(Instant.now())
                            .errorCode("VALUE_TOO_LONG")
                            .message("Значение поля превышает допустимую длину")
                            .detail(detail)
                            .build());
        }
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.builder()
                        .timestamp(Instant.now())
                        .errorCode("DATA_CONFLICT")
                        .message("Данные конфликтуют с текущим состоянием")
                        .detail(detail)
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .timestamp(Instant.now())
                        .errorCode("INTERNAL_ERROR")
                        .message("Внутренняя ошибка сервера")
                        .build());
    }

    private ErrorResponse toResponse(BusinessException ex) {
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .errorCode(ex.getErrorCode().name())
                .message(ex.getMessage())
                .build();
    }

    private ErrorDetail toDetail(FieldError fieldError) {
        return ErrorDetail.builder()
                .field(fieldError.getField())
                .message(fieldError.getDefaultMessage())
                .build();
    }
}
