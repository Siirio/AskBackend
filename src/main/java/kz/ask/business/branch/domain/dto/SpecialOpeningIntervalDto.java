package kz.ask.business.branch.domain.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SpecialOpeningIntervalDto {

    private LocalDate date;
    private Boolean closed;
    private LocalTime opensAt;
    private LocalTime closesAt;
}
