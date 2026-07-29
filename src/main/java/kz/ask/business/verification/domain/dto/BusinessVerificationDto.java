package kz.ask.business.verification.domain.dto;

import java.util.UUID;
import kz.ask.business.verification.domain.enums.VerificationStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BusinessVerificationDto {

    private UUID id;
    private UUID businessId;
    private VerificationStatus status;
    private String twoGisUrl;
    private String kaspiUrl;
    private String ozonUrl;
    private String wildberriesUrl;
    private String websiteUrl;
    private String instagramUrl;
    private String telegramUrl;
    private String phone;
    private String corporateEmail;
}
