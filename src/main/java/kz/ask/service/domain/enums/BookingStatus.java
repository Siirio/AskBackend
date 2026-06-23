package kz.ask.service.domain.enums;

public enum BookingStatus {
    PENDING,
    CONFIRMED_BY_USER,
    COMPLETED_BY_USER,
    CANCELLED_BY_USER,
    CONFIRMED_BY_BUSINESS,
    DECLINED_BY_BUSINESS,
    SUGGEST_OTHER_TIME_BY_BUSINESS
}
