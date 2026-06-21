package kz.ask.identity.domain;

import java.util.UUID;

public class AuthChallengeInvalidCodeException extends RuntimeException {

    public AuthChallengeInvalidCodeException(UUID id) {
        super("Invalid code for challenge: " + id);
    }
}
