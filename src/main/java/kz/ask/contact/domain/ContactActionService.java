package kz.ask.contact.domain;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import kz.ask.business.domain.entity.BusinessContact;
import kz.ask.business.domain.enums.ContactType;
import kz.ask.business.domain.enums.ContactVisibility;
import kz.ask.business.infrastructure.repository.BusinessContactRepository;
import kz.ask.contact.api.dto.ContactActionSummaryResponse;
import kz.ask.contact.api.dto.ContactResolveResponse;
import kz.ask.shared.domain.enums.RecordStatus;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.NotFoundException;
import kz.ask.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ContactActionService {

    private static final String TOKEN_SEPARATOR = ".";
    private static final Integer MAX_CARD_ACTIONS = 3;

    private final BusinessContactRepository businessContactRepository;
    private final ContactCryptoService contactCryptoService;

    @Value("${ask.contact.action-token-ttl:30m}")
    private String actionTokenTtl;

    public List<ContactActionSummaryResponse> summarize(UUID businessId) {
        return businessContactRepository.findByBusinessIdAndStatusOrderByPrimaryContactDescUpdatedAtDesc(
                        businessId, RecordStatus.ACTIVE)
                .stream()
                .filter(contact -> contact.getVisibility() != ContactVisibility.INTERNAL)
                .limit(MAX_CARD_ACTIONS)
                .map(this::toSummary)
                .toList();
    }

    public ContactResolveResponse resolve(String contactActionId) {
        DecodedContactAction decoded = decode(contactActionId);
        if (decoded.expiresAt().isBefore(Instant.now())) {
            throw new ValidationException(ErrorCode.CONTACT_ACTION_EXPIRED);
        }
        BusinessContact contact = businessContactRepository.findById(decoded.contactId())
                .filter(found -> found.getStatus() == RecordStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CONTACT_NOT_FOUND));
        String value = resolveValue(contact);
        return ContactResolveResponse.builder()
                .actionType(actionType(contact.getContactType()))
                .redirectUrl(redirectUrl(contact.getContactType(), value))
                .deepLink(deepLink(contact.getContactType(), value))
                .displayValue(displayValue(contact.getContactType(), value, contact.getDisplayValue()))
                .provider(provider(contact.getContactType()))
                .label(label(contact.getContactType()))
                .expiresAt(decoded.expiresAt())
                .build();
    }

    private ContactActionSummaryResponse toSummary(BusinessContact contact) {
        return ContactActionSummaryResponse.builder()
                .contactActionId(encode(contact.getId()))
                .provider(provider(contact.getContactType()))
                .label(label(contact.getContactType()))
                .build();
    }

    private String encode(UUID contactId) {
        Instant expiresAt = Instant.now().plus(DurationStyle.detectAndParse(actionTokenTtl));
        String payload = contactId + "|" + expiresAt.toEpochMilli();
        String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        String signature = contactCryptoService.sign(encodedPayload);
        return encodedPayload + TOKEN_SEPARATOR + signature;
    }

    private DecodedContactAction decode(String token) {
        String[] parts = token.split("\\" + TOKEN_SEPARATOR, 2);
        if (parts.length != 2 || !contactCryptoService.sign(parts[0]).equals(parts[1])) {
            throw new ValidationException(ErrorCode.CONTACT_ACTION_INVALID);
        }
        try {
            String payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            String[] payloadParts = payload.split("\\|", 2);
            if (payloadParts.length != 2) {
                throw new ValidationException(ErrorCode.CONTACT_ACTION_INVALID);
            }
            return new DecodedContactAction(UUID.fromString(payloadParts[0]),
                    Instant.ofEpochMilli(Long.parseLong(payloadParts[1])));
        } catch (IllegalArgumentException ex) {
            throw new ValidationException(ErrorCode.CONTACT_ACTION_INVALID);
        }
    }

    private String resolveValue(BusinessContact contact) {
        if (StringUtils.hasText(contact.getEncryptedValue())) {
            return contactCryptoService.decrypt(contact.getEncryptedValue());
        }
        return contact.getContactValue();
    }

    private String provider(ContactType type) {
        return switch (type) {
            case WHATSAPP -> "WHATSAPP";
            case TELEGRAM -> "TELEGRAM";
            case INSTAGRAM -> "INSTAGRAM";
            case WEBSITE, SITE -> "SITE";
            case TWO_GIS -> "TWO_GIS";
            case EMAIL -> "EMAIL";
            case PHONE -> "PHONE";
        };
    }

    private String label(ContactType type) {
        return switch (type) {
            case WHATSAPP -> "Написать в WhatsApp";
            case TELEGRAM -> "Написать в Telegram";
            case INSTAGRAM -> "Открыть Instagram";
            case WEBSITE, SITE -> "Открыть сайт";
            case TWO_GIS -> "Открыть 2GIS";
            case EMAIL -> "Написать email";
            case PHONE -> "Показать телефон";
        };
    }

    private String actionType(ContactType type) {
        return switch (type) {
            case PHONE -> "DISPLAY";
            case TELEGRAM -> "DEEP_LINK";
            default -> "REDIRECT";
        };
    }

    private String redirectUrl(ContactType type, String value) {
        return switch (type) {
            case WHATSAPP -> "https://wa.me/" + digits(value);
            case INSTAGRAM -> "https://instagram.com/" + username(value);
            case WEBSITE, SITE, TWO_GIS -> value;
            case EMAIL -> "mailto:" + value;
            default -> null;
        };
    }

    private String deepLink(ContactType type, String value) {
        if (type == ContactType.TELEGRAM) {
            return "tg://resolve?domain=" + username(value);
        }
        return null;
    }

    private String displayValue(ContactType type, String value, String displayValue) {
        if (type != ContactType.PHONE) {
            return null;
        }
        return StringUtils.hasText(displayValue) ? displayValue : value;
    }

    private String username(String value) {
        String trimmed = value == null ? "" : value.trim();
        int index = trimmed.lastIndexOf("/");
        String candidate = index >= 0 ? trimmed.substring(index + 1) : trimmed;
        return candidate.replace("@", "");
    }

    private String digits(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }

    private record DecodedContactAction(UUID contactId, Instant expiresAt) {
    }
}
