package kz.ask.business.branch.domain.entity;

import jakarta.persistence.Embeddable;
import java.time.DayOfWeek;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class WeeklyOpeningInterval {

    private DayOfWeek dayOfWeek;
    private LocalTime opensAt;
    private LocalTime closesAt;
}
