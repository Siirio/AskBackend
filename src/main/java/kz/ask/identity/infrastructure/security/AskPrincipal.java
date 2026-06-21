package kz.ask.identity.infrastructure.security;

import java.util.UUID;
import kz.ask.identity.domain.enums.AppRole;

public class AskPrincipal {

    private final UUID userId;
    private final UUID sessionId;
    private final String displayName;
    private final AppRole role;

    public AskPrincipal(UUID userId, UUID sessionId, String displayName, AppRole role) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.displayName = displayName;
        this.role = role;
    }

    public UUID getUserId() { return userId; }
    public UUID getSessionId() { return sessionId; }
    public String getDisplayName() { return displayName; }
    public AppRole getRole() { return role; }
}
