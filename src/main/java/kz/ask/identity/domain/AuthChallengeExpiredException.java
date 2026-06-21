package kz.ask.identity.domain;

import java.util.UUID;

public class AuthChallengeExpiredException extends RuntimeException {

    public AuthChallengeExpiredException(UUID id) {
        super("Challenge expired: " + id);
    }
}
