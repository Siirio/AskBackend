package kz.ask.identity.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {
    private String displayName;
    private String email;
    private String phone;
}
