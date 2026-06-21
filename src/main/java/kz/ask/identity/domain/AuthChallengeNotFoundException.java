package kz.ask.identity.domain;

import java.util.UUID;

public class AuthChallengeNotFoundException extends RuntimeException {

    public AuthChallengeNotFoundException(UUID id) {
        super("Challenge not found: " + id);
    }
}
