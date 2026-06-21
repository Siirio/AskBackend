package kz.ask.identity.api.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class VerifyCodeRequest {

    @NotNull
    private UUID authChallengeId;
    @NotBlank
    @Size(min = 6, max = 6)
    @Pattern(regexp = "\\d{6}")
    private String code;

    public UUID getAuthChallengeId() { return authChallengeId; }
    public void setAuthChallengeId(UUID authChallengeId) { this.authChallengeId = authChallengeId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
