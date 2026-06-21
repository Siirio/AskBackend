package kz.ask.identity.api.dto;

public class LogoutResponse {

    private boolean success;

    public LogoutResponse(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}
