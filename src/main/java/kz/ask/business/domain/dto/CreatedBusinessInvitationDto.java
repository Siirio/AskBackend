package kz.ask.business.domain.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CreatedBusinessInvitationDto {

    private BusinessInvitationDto invitation;
    private String rawToken;
}
