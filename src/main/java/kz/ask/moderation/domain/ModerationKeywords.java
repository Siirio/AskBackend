package kz.ask.moderation.domain;

import java.util.List;
import java.util.Set;

public final class ModerationKeywords {

    private ModerationKeywords() {
    }

    private static final Set<String> PROHIBITED = Set.of(
            "секс", "sex", "секс-игрушка", "sex toy", "фаллоимитатор", "dildo", "вибратор", "vibrator",
            "18+", "порно", "porn", "эротик", "erotic", "интим", "intimate",
            "курение", "smoking", "сигарет", "cigarette", "табак", "tobacco", "вейп", "vape",
            "кальян", "hookah", "зажигалк", "lighter",
            "алкоголь", "alcohol", "водка", "vodka", "пиво", "beer", "вино", "wine",
            "коньяк", "cognac", "виски", "whiskey", "шампанское", "champagne", "ликер", "liqueur",
            "наркотик", "drug", "марихуан", "marijuana", "каннабис", "cannabis",
            "трава", "weed", "кокаин", "cocaine", "героин", "heroin",
            "нож", "knife", "меч", "sword", "пистолет", "pistol", "gun", "ружье", "rifle",
            "винтовка", "автомат", "сабля", "saber", "sabre", "кинжал", "dagger",
            "шпага", "rapier", "арбалет", "crossbow", "лук", "bow", "катана", "katana",
            "мачете", "machete", "топор", "axe", "копье", "spear", "дубинка", "batton"
    );

    public static String prohibitedMatch(String text) {
        return matchAny(text, PROHIBITED);
    }

    private static String matchAny(String text, Set<String> keywords) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String lower = text.toLowerCase();
        for (String keyword : keywords) {
            if (lower.contains(keyword)) {
                return keyword;
            }
        }
        return null;
    }

    public static List<String> prohibitedKeywords() {
        return List.copyOf(PROHIBITED);
    }
}
