package kz.ask.business.branch.domain;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import kz.ask.business.branch.domain.dto.BusinessBranchDto;
import kz.ask.business.branch.domain.dto.BranchOpeningSummary;
import kz.ask.business.branch.domain.dto.SpecialOpeningIntervalDto;
import kz.ask.business.branch.domain.dto.WeeklyOpeningIntervalDto;
import org.springframework.stereotype.Component;

@Component
public class BranchOpeningHoursPolicy {

    private final Clock clock;

    public BranchOpeningHoursPolicy(Clock clock) {
        this.clock = clock;
    }

    public BranchOpeningSummary evaluate(BusinessBranchDto branch) {
        Instant now = clock.instant();
        if (branch.getTimeZoneId() == null || branch.getWeeklyHours() == null
                || branch.getWeeklyHours().isEmpty()) {
            return BranchOpeningSummary.builder()
                    .state("UNKNOWN")
                    .timeZoneId(branch.getTimeZoneId())
                    .evaluatedAt(now)
                    .build();
        }

        ZoneId zoneId;
        try {
            zoneId = ZoneId.of(branch.getTimeZoneId());
        } catch (Exception e) {
            return BranchOpeningSummary.builder()
                    .state("UNKNOWN")
                    .timeZoneId(branch.getTimeZoneId())
                    .evaluatedAt(now)
                    .build();
        }

        ZonedDateTime zonedNow = now.atZone(zoneId);
        LocalDate today = zonedNow.toLocalDate();
        LocalTime currentTime = zonedNow.toLocalTime();
        DayOfWeek currentDay = today.getDayOfWeek();

        SpecialOpeningIntervalDto specialOverride = findSpecialOverride(branch.getSpecialHours(), today);
        if (specialOverride != null) {
            if (Boolean.TRUE.equals(specialOverride.getClosed())) {
                Instant nextOpens = findNextWeeklyOpen(branch.getWeeklyHours(), today, zoneId,
                        branch.getSpecialHours());
                return BranchOpeningSummary.builder()
                        .state("CLOSED")
                        .timeZoneId(branch.getTimeZoneId())
                        .evaluatedAt(now)
                        .nextOpensAt(nextOpens)
                        .build();
            }
            return evaluateInterval(specialOverride.getOpensAt(), specialOverride.getClosesAt(),
                    currentTime, today, zoneId, branch);
        }

        return evaluateWeekly(currentDay, currentTime, today, zoneId, branch);
    }

    private SpecialOpeningIntervalDto findSpecialOverride(
            List<SpecialOpeningIntervalDto> specialHours, LocalDate date) {
        if (specialHours == null) return null;
        return specialHours.stream()
                .filter(s -> date.equals(s.getDate()))
                .findFirst()
                .orElse(null);
    }

    private BranchOpeningSummary evaluateWeekly(DayOfWeek currentDay, LocalTime currentTime,
                                                 LocalDate today, ZoneId zoneId,
                                                 BusinessBranchDto branch) {
        WeeklyOpeningIntervalDto todaySchedule = branch.getWeeklyHours().stream()
                .filter(w -> w.getDayOfWeek() == currentDay)
                .findFirst()
                .orElse(null);

        if (todaySchedule != null) {
            return evaluateInterval(todaySchedule.getOpensAt(), todaySchedule.getClosesAt(),
                    currentTime, today, zoneId, branch);
        }

        Instant nextOpens = findNextWeeklyOpen(branch.getWeeklyHours(), today, zoneId,
                branch.getSpecialHours());
        return BranchOpeningSummary.builder()
                .state("CLOSED")
                .timeZoneId(branch.getTimeZoneId())
                .evaluatedAt(clock.instant())
                .nextOpensAt(nextOpens)
                .build();
    }

    private BranchOpeningSummary evaluateInterval(LocalTime opensAt, LocalTime closesAt,
                                                   LocalTime currentTime, LocalDate today,
                                                   ZoneId zoneId, BusinessBranchDto branch) {
        Instant now = clock.instant();
        if (opensAt == null || closesAt == null) {
            return BranchOpeningSummary.builder()
                    .state("UNKNOWN")
                    .timeZoneId(branch.getTimeZoneId())
                    .evaluatedAt(now)
                    .build();
        }

        if (!currentTime.isBefore(opensAt) && currentTime.isBefore(closesAt)) {
            Instant nextCloses = ZonedDateTime.of(today, closesAt, zoneId).toInstant();
            if (nextCloses.isBefore(now)) {
                nextCloses = ZonedDateTime.of(today.plusDays(1), closesAt, zoneId).toInstant();
            }
            return BranchOpeningSummary.builder()
                    .state("OPEN")
                    .timeZoneId(branch.getTimeZoneId())
                    .evaluatedAt(now)
                    .nextClosesAt(nextCloses)
                    .build();
        }

        Instant nextOpens;
        if (currentTime.isBefore(opensAt)) {
            nextOpens = ZonedDateTime.of(today, opensAt, zoneId).toInstant();
        } else {
            nextOpens = findNextWeeklyOpen(branch.getWeeklyHours(), today, zoneId,
                    branch.getSpecialHours());
        }

        return BranchOpeningSummary.builder()
                .state("CLOSED")
                .timeZoneId(branch.getTimeZoneId())
                .evaluatedAt(now)
                .nextOpensAt(nextOpens)
                .build();
    }

    private Instant findNextWeeklyOpen(List<WeeklyOpeningIntervalDto> weeklyHours,
                                        LocalDate afterDate, ZoneId zoneId,
                                        List<SpecialOpeningIntervalDto> specialHours) {
        LocalDate searchDate = afterDate.plusDays(1);
        for (int i = 0; i < 14; i++) {
            LocalDate d = searchDate.plusDays(i);
            SpecialOpeningIntervalDto special = findSpecialOverride(specialHours, d);
            if (special != null) {
                if (Boolean.TRUE.equals(special.getClosed())) continue;
                if (special.getOpensAt() != null) {
                    return ZonedDateTime.of(d, special.getOpensAt(), zoneId).toInstant();
                }
                continue;
            }
            DayOfWeek dow = d.getDayOfWeek();
            WeeklyOpeningIntervalDto weekly = weeklyHours.stream()
                    .filter(w -> w.getDayOfWeek() == dow)
                    .findFirst()
                    .orElse(null);
            if (weekly != null && weekly.getOpensAt() != null) {
                return ZonedDateTime.of(d, weekly.getOpensAt(), zoneId).toInstant();
            }
        }
        return null;
    }
}
