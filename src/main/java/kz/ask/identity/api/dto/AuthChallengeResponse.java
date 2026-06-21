package kz.ask.identity.api.dto;

import java.time.Instant;
import java.util.UUID;

public class AuthChallengeResponse {

    private UUID authChallengeId;
    private String role;
    private String purpose;
    private String channel;
    private String maskedDestination;
    private Instant expiresAt;

    public UUID getAuthChallengeId() { return authChallengeId; }
    public void setAuthChallengeId(UUID authChallengeId) { this.authChallengeId = authChallengeId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getMaskedDestination() { return maskedDestination; }
    public void setMaskedDestination(String maskedDestination) { this.maskedDestination = maskedDestination; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
}
