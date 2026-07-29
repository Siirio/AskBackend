package kz.ask.moderation.domain;

public record ModerationAssessment(
        Decision decision,
        String reasonCode,
        String matchedSignal) {

    public enum Decision {
        CLEAR,
        REVIEW,
        BLOCK
    }

    public static ModerationAssessment clear() {
        return new ModerationAssessment(Decision.CLEAR, null, null);
    }

    public boolean requiresAction() {
        return decision != Decision.CLEAR;
    }
}
