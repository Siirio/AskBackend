package kz.ask.identity.application;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BusinessRegistrationPayload {

    private String businessName;
    private String branchName;
    private UUID branchCityId;
    private String branchAddress;
    private Boolean onlineOnly;
    private String email;
    private String phone;
}
