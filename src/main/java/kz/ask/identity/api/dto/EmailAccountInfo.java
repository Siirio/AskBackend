package kz.ask.identity.api.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EmailAccountInfo {

    private String role;
    private String status;
    private String businessName;
}
