package kz.ask.business.branch.domain.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WeeklyOpeningIntervalDto {

    private DayOfWeek dayOfWeek;
    private LocalTime opensAt;
    private LocalTime closesAt;
}
