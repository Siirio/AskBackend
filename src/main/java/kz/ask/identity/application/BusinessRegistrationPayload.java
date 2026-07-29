package kz.ask.identity.application;

import java.util.UUID;
import kz.ask.business.core.domain.enums.BusinessScope;
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
    private UUID businessCategoryId;
    private String businessCategoryName;
    private BusinessScope businessScope;
    private String email;
    private String countryCode;
    private String locale;
}
