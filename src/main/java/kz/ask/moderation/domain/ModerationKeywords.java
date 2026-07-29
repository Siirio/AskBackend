package kz.ask.moderation.domain;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class ModerationKeywords {

    private static final String CRITICAL_REASON = "AUTO_CRITICAL_ILLEGAL_CONTENT";
    private static final String REVIEW_REASON = "AUTO_REVIEW_REGULATED_CONTENT";
    private static final Pattern OBFUSCATED_WORD =
            Pattern.compile("(?iu)(?<!\\p{L})(?:\\p{L}\\s+){2,}\\p{L}(?!\\p{L})");

    private static final List<Signal> CRITICAL_SIGNALS = List.of(
            signal("illegal drugs", "наркотик\\p{L}*|кокаин\\p{L}*|героин\\p{L}*|мефедрон\\p{L}*|"
                    + "метамфетамин\\p{L}*|экстази|марихуан\\p{L}*|каннабис\\p{L}*|"
                    + "cocaine|c[o0]caine|heroin|mephedrone|methamphetamine|mdma|marijuana|cannabis|weed"),
            signal("firearms or explosives", "огнестрельн\\p{L}*|боеприпас\\p{L}*|взрывчат\\p{L}*|"
                    + "боев\\p{L}*\\s+гранат\\p{L}*|автомат\\s+калашников\\p{L}*|"
                    + "firearm\\p{L}*|ammunition|explosive\\p{L}*"),
            signal("explicit sexual content", "порно\\p{L}*|п[о0]рно\\p{L}*|проститу\\p{L}*|"
                    + "интимн\\p{L}*\\s+услуг\\p{L}*|porn\\p{L}*|p[o0]rn\\p{L}*|"
                    + "prostitution|escort\\s+service\\p{L}*")
    );

    private static final List<Signal> REVIEW_SIGNALS = List.of(
            signal("adult products", "с[е3]кс[\\s-]*игрушк\\p{L}*|фаллоимитатор\\p{L}*|"
                    + "вибратор\\p{L}*|товар\\p{L}*\\s+18\\+|sex\\s*toy\\p{L}*|"
                    + "dildo\\p{L}*|v[i1]brator\\p{L}*"),
            signal("alcohol", "алкогол\\p{L}*|водк\\p{L}*|пив\\p{L}*|"
                    + "вино|вина|винн\\p{L}*|коньяк\\p{L}*|"
                    + "виск\\p{L}*|шампанск\\p{L}*|ликер\\p{L}*|alcohol|vodka|beer|wine|"
                    + "cognac|whisk(?:y|ey)|champagne|liqueur"),
            signal("tobacco or vaping", "сигарет\\p{L}*|табак\\p{L}*|вейп\\p{L}*|кальян\\p{L}*|"
                    + "никотин\\p{L}*|cigarette\\p{L}*|tobacco|vape\\p{L}*|hookah|nicotine"),
            signal("regulated weapon", "нож(?!ниц)\\p{L}*|меч\\p{L}*|пистолет\\p{L}*|ружь\\p{L}*|"
                    + "винтовк\\p{L}*|сабл\\p{L}*|оружи\\p{L}*|"
                    + "кинжал\\p{L}*|арбалет\\p{L}*|катан\\p{L}*|мачет\\p{L}*|топор\\p{L}*|"
                    + "спортивн\\p{L}*\\s+лук|охотнич\\p{L}*\\s+лук|лук\\s+для\\s+стрельбы|"
                    + "knife|sword|pistol|gun|rifle|dagger|crossbow|katana|machete"),
            signal("counterfeit or document evasion", "реплик\\p{L}*\\s+бренд\\p{L}*|копи\\p{L}*\\s+бренд\\p{L}*|"
                    + "подделк\\p{L}*|контрафакт\\p{L}*|без\\s+документ\\p{L}*|fake\\s+brand|"
                    + "counterfeit|without\\s+documents"),
            signal("gambling", "казино|букмекер\\p{L}*|ставк\\p{L}*\\s+на\\s+спорт|casino|sports\\s+betting")
    );

    private ModerationKeywords() {
    }

    public static ModerationAssessment assess(String... values) {
        String text = removeSafeContexts(normalize(values));
        if (text.isBlank()) {
            return ModerationAssessment.clear();
        }
        Signal critical = find(text, CRITICAL_SIGNALS);
        if (critical != null) {
            return new ModerationAssessment(
                    ModerationAssessment.Decision.BLOCK,
                    CRITICAL_REASON,
                    critical.label());
        }
        Signal review = find(text, REVIEW_SIGNALS);
        if (review != null) {
            return new ModerationAssessment(
                    ModerationAssessment.Decision.REVIEW,
                    REVIEW_REASON,
                    review.label());
        }
        return ModerationAssessment.clear();
    }

    public static String prohibitedMatch(String text) {
        ModerationAssessment assessment = assess(text);
        return assessment.requiresAction() ? assessment.matchedSignal() : null;
    }

    public static List<String> prohibitedKeywords() {
        return CRITICAL_SIGNALS.stream().map(Signal::label)
                .collect(java.util.stream.Collectors.toList());
    }

    private static Signal find(String text, List<Signal> signals) {
        return signals.stream()
                .filter(signal -> signal.pattern().matcher(text).find())
                .findFirst()
                .orElse(null);
    }

    private static Signal signal(String label, String alternatives) {
        return new Signal(
                label,
                Pattern.compile(
                        "(?iu)(?<![\\p{L}\\p{N}])(?:" + alternatives + ")(?![\\p{L}\\p{N}])"));
    }

    private static String normalize(String... values) {
        String joined = java.util.Arrays.stream(values)
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .collect(java.util.stream.Collectors.joining(" "));
        String normalized = Normalizer.normalize(joined, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT)
                .replace('ё', 'е')
                .replaceAll("[^\\p{L}\\p{N}+\\-]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
        return collapseObfuscatedWords(normalized);
    }

    private static String removeSafeContexts(String text) {
        return text
                .replaceAll("(?iu)лечени\\p{L}*\\s+(?:от\\s+)?наркотическ\\p{L}*\\s+зависимост\\p{L}*", " ")
                .replaceAll("(?iu)реабилитац\\p{L}*\\s+(?:при\\s+)?наркотическ\\p{L}*\\s+зависимост\\p{L}*", " ")
                .replaceAll("(?iu)drug\\s+rehabilitation|addiction\\s+treatment", " ");
    }

    private static String collapseObfuscatedWords(String text) {
        java.util.regex.Matcher matcher = OBFUSCATED_WORD.matcher(text);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(
                    result,
                    java.util.regex.Matcher.quoteReplacement(matcher.group().replace(" ", "")));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private record Signal(String label, Pattern pattern) {
    }
}
