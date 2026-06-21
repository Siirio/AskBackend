package kz.ask.identity.api;

import java.time.Instant;
import java.util.Map;
import kz.ask.business.domain.BusinessRegistrationException;
import kz.ask.identity.application.AuthContactTakenException;
import kz.ask.identity.application.AuthRegistrationPayloadException;
import kz.ask.identity.application.AuthRoleMismatchException;
import kz.ask.identity.application.AuthSessionInvalidException;
import kz.ask.identity.application.AuthUserNotFoundException;
import kz.ask.identity.domain.AuthChallengeExpiredException;
import kz.ask.identity.domain.AuthChallengeInvalidCodeException;
import kz.ask.identity.domain.AuthChallengeMaxAttemptsException;
import kz.ask.identity.domain.AuthChallengeNotFoundException;
import kz.ask.identity.domain.VerificationDeliveryException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = AuthController.class)
class AuthExceptionHandler {

    @ExceptionHandler({
        AuthUserNotFoundException.class,
        AuthChallengeNotFoundException.class
    })
    public ResponseEntity<Map<String, Object>> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
            "error", HttpStatus.NOT_FOUND.value(),
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler(AuthContactTakenException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
            "error", HttpStatus.CONFLICT.value(),
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler({
        AuthChallengeExpiredException.class,
        AuthChallengeMaxAttemptsException.class,
        AuthChallengeInvalidCodeException.class
    })
    public ResponseEntity<Map<String, Object>> handleUnprocessable(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of(
            "error", HttpStatus.UNPROCESSABLE_ENTITY.value(),
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler({
        AuthRegistrationPayloadException.class,
        BusinessRegistrationException.class
    })
    public ResponseEntity<Map<String, Object>> handleBadRegistrationState(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of(
            "error", HttpStatus.UNPROCESSABLE_ENTITY.value(),
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler({
        AuthRoleMismatchException.class,
        AuthSessionInvalidException.class
    })
    public ResponseEntity<Map<String, Object>> handleForbidden(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
            "error", HttpStatus.FORBIDDEN.value(),
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler(VerificationDeliveryException.class)
    public ResponseEntity<Map<String, Object>> handleDeliveryUnavailable(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
            "error", HttpStatus.SERVICE_UNAVAILABLE.value(),
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString()
        ));
    }
}
