package kz.ask.business.domain.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BusinessDto {

    private UUID id;
    private String name;
}
