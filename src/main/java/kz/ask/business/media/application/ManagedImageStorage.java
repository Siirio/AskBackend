package kz.ask.business.media.application;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import kz.ask.business.media.infrastructure.BusinessMediaStorage;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class ManagedImageStorage {

    public static final String FILE_URL_PREFIX = "/api/v1/business-media/files/";

    private final BusinessMediaStorage businessMediaStorage;

    @Value("${ask.business-media.max-file-size:5242880}")
    private Long maxFileSize;

    @Value("${ask.business-media.allowed-extensions:png,jpg,jpeg,webp}")
    private String allowedExtensionsConfig;

    @Value("${ask.business-media.allowed-content-types:image/png,image/jpeg,image/webp}")
    private String allowedContentTypesConfig;

    public String store(MultipartFile file) {
        String extension = extension(file.getOriginalFilename());
        String contentType = file.getContentType();
        if (file.isEmpty()
                || file.getSize() > maxFileSize
                || !splitConfig(allowedExtensionsConfig).contains(extension)
                || contentType == null
                || !splitConfig(allowedContentTypesConfig).contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new ValidationException(ErrorCode.FILE_INVALID);
        }
        String storedName = UUID.randomUUID() + "." + extension;
        businessMediaStorage.store(file, storedName, extension);
        return storedName;
    }

    public void delete(String storedName) {
        if (storedName != null && !storedName.isBlank()) {
            businessMediaStorage.delete(storedName);
        }
    }

    public String publicUrl(String storedName) {
        return storedName == null ? null : FILE_URL_PREFIX + storedName;
    }

    public String storedName(String fileUrl) {
        return fileUrl != null && fileUrl.startsWith(FILE_URL_PREFIX)
                ? fileUrl.substring(FILE_URL_PREFIX.length()) : null;
    }

    private Set<String> splitConfig(String value) {
        return Arrays.stream(value.split(","))
                .map(item -> item.trim().toLowerCase(Locale.ROOT))
                .filter(item -> !item.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    private String extension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
