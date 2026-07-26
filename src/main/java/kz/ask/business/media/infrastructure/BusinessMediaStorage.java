package kz.ask.business.media.infrastructure;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.InternalServerException;
import kz.ask.shared.error.ValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class BusinessMediaStorage {

    private static final Map<String, byte[]> MAGIC_BYTES = Map.of(
            "png", new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47},
            "jpg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
            "jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
            "webp", new byte[]{0x52, 0x49, 0x46, 0x46});

    private final Path uploadDir;

    public BusinessMediaStorage(
            @Value("${ask.business-media.upload-dir:uploads/business-media}") String uploadDirPath)
            throws IOException {
        this.uploadDir = Paths.get(uploadDirPath).toAbsolutePath().normalize();
        Files.createDirectories(this.uploadDir);
    }

    public void store(MultipartFile file, String storedName, String extension) {
        Path target = resolvePath(storedName);
        try {
            file.transferTo(target.toFile());
            validateContent(target, extension);
        } catch (IOException e) {
            deleteQuietly(target);
            throw new InternalServerException(ErrorCode.FILE_INVALID);
        } catch (ValidationException e) {
            deleteQuietly(target);
            throw e;
        }
    }

    public Resource resolve(String storedName) {
        Path file = resolvePath(storedName);
        try {
            Resource resource = new UrlResource(file.toUri());
            return resource.exists() && resource.isReadable() ? resource : null;
        } catch (IOException e) {
            return null;
        }
    }

    public void delete(String storedName) {
        deleteQuietly(resolvePath(storedName));
    }

    private Path resolvePath(String storedName) {
        Path target = uploadDir.resolve(storedName).normalize();
        if (!target.startsWith(uploadDir)) {
            throw new ValidationException(ErrorCode.FILE_INVALID);
        }
        return target;
    }

    private void validateContent(Path target, String extension) throws IOException {
        byte[] head = readHead(target);
        byte[] expected = MAGIC_BYTES.get(extension);
        if (expected == null || !startsWith(head, expected) || "webp".equals(extension) && !isWebp(head)) {
            throw new ValidationException(ErrorCode.FILE_INVALID);
        }
    }

    private byte[] readHead(Path target) throws IOException {
        try (InputStream input = Files.newInputStream(target)) {
            return input.readNBytes(12);
        }
    }

    private boolean isWebp(byte[] content) {
        return content.length >= 12
                && content[8] == 0x57
                && content[9] == 0x45
                && content[10] == 0x42
                && content[11] == 0x50;
    }

    private boolean startsWith(byte[] content, byte[] prefix) {
        if (content.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (content[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    private void deleteQuietly(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
            return;
        }
    }
}
