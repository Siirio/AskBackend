package kz.ask.identity.domain;

import java.util.UUID;

public class AuthChallengeMaxAttemptsException extends RuntimeException {

    public AuthChallengeMaxAttemptsException(UUID id) {
        super("Challenge max attempts reached: " + id);
    }
}
