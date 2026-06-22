package kz.ask.business.domain.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BusinessMemberDto {

    private UUID id;
    private UUID businessId;
    private String role;
}
