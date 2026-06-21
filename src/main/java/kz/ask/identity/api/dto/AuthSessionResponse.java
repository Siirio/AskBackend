package kz.ask.identity.api.dto;

import java.time.Instant;

public class AuthSessionResponse {

    private String accessToken;
    private String tokenType;
    private Instant expiresAt;
    private boolean remembered;
    private String role;
    private String startRoute;
    private AuthUserResponse user;
    private AuthBusinessContextResponse business;

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public boolean isRemembered() { return remembered; }
    public void setRemembered(boolean remembered) { this.remembered = remembered; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getStartRoute() { return startRoute; }
    public void setStartRoute(String startRoute) { this.startRoute = startRoute; }
    public AuthUserResponse getUser() { return user; }
    public void setUser(AuthUserResponse user) { this.user = user; }
    public AuthBusinessContextResponse getBusiness() { return business; }
    public void setBusiness(AuthBusinessContextResponse business) { this.business = business; }
}
