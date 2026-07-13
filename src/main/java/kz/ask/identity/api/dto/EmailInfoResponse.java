package kz.ask.identity.api.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EmailInfoResponse {

    private Boolean exists;
    private List<EmailAccountInfo> accounts;
}
