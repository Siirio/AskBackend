package kz.ask.identity.infrastructure.security;

import java.util.UUID;

public class AskPrincipal {

    private final UUID userId;
    private final UUID sessionId;
    private final String displayName;
    private final String authority;

    public AskPrincipal(UUID userId, UUID sessionId, String displayName, String authority) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.displayName = displayName;
        this.authority = authority;
    }

    public UUID getUserId() { return userId; }
    public UUID getSessionId() { return sessionId; }
    public String getDisplayName() { return displayName; }
    public String getAuthority() { return authority; }
}
