package kz.ask.managedimport.api.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import java.util.regex.Pattern;
import kz.ask.business.core.domain.enums.ImportSourceType;
import kz.ask.business.core.domain.enums.BusinessScope;
import kz.ask.business.core.domain.enums.PreferredContactChannel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateManagedImportRequest {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern TELEGRAM_PATTERN = Pattern.compile("^@[A-Za-z0-9_]{5,32}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9][0-9\\s()\\-]{7,20}$");

    @NotNull
    private BusinessScope businessScope;

    private Set<ImportSourceType> selectedSourceTypes;

    @NotNull
    private PreferredContactChannel preferredContactChannel;

    @NotBlank
    private String preferredContactValue;

    private String sourceLinks;

    private String sourceNotes;

    @AssertTrue(message = "Contact does not match the selected channel")
    public boolean isPreferredContactValid() {
        if (preferredContactChannel == null || preferredContactValue == null
                || preferredContactValue.isBlank()) {
            return true;
        }
        String value = preferredContactValue.trim();
        return switch (preferredContactChannel) {
            case EMAIL -> EMAIL_PATTERN.matcher(value).matches();
            case TELEGRAM -> TELEGRAM_PATTERN.matcher(value).matches();
            case WHATSAPP -> isValidPhone(value);
        };
    }

    private boolean isValidPhone(String value) {
        if (!PHONE_PATTERN.matcher(value).matches()) {
            return false;
        }
        int digitCount = value.replaceAll("\\D", "").length();
        return digitCount >= 8 && digitCount <= 15;
    }
}
