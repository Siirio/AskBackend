package kz.ask.business.branch.domain.entity;

import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class SpecialOpeningInterval {

    private LocalDate date;
    private Boolean closed;
    private LocalTime opensAt;
    private LocalTime closesAt;
}
