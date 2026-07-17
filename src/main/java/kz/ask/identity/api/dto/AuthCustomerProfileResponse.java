package kz.ask.identity.api.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuthCustomerProfileResponse {

    private Boolean enabled;
}
