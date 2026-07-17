package kz.ask.chat.infrastructure;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.InternalServerException;
import kz.ask.shared.error.ValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ChatFileStorage {

    private static final Map<String, byte[]> MAGIC_BYTES = Map.of(
            "png", new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47},
            "jpg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
            "jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
            "pdf", new byte[]{0x25, 0x50, 0x44, 0x46},
            "xlsx", new byte[]{0x50, 0x4B, 0x03, 0x04});

    private static final Set<String> FORBIDDEN_TEXT_PREFIXES = Set.of(
            "<!doctype", "<html", "<svg", "<script", "<?xml");

    private final Path uploadDir;

    public ChatFileStorage(
            @Value("${ask.chat.upload-dir:uploads/chat}") String uploadDirPath) throws IOException {
        this.uploadDir = Paths.get(uploadDirPath).toAbsolutePath().normalize();
        Files.createDirectories(this.uploadDir);
    }

    public void store(MultipartFile file, String storedName, String extension) {
        Path target = uploadDir.resolve(storedName).normalize();
        if (!target.startsWith(uploadDir)) {
            throw new ValidationException(ErrorCode.FILE_INVALID);
        }
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
        Path file = uploadDir.resolve(storedName).normalize();
        if (!file.startsWith(uploadDir)) {
            return null;
        }
        try {
            Resource resource = new UrlResource(file.toUri());
            return resource.exists() && resource.isReadable() ? resource : null;
        } catch (IOException e) {
            return null;
        }
    }

    public void delete(String storedName) {
        Path file = uploadDir.resolve(storedName).normalize();
        if (file.startsWith(uploadDir)) {
            deleteQuietly(file);
        }
    }

    private void validateContent(Path target, String extension) throws IOException {
        byte[] head = readHead(target);
        byte[] expected = MAGIC_BYTES.get(extension);
        if (expected != null) {
            if (!startsWith(head, expected)) {
                throw new ValidationException(ErrorCode.FILE_INVALID);
            }
            return;
        }
        validateTextContent(head);
    }

    private void validateTextContent(byte[] head) {
        for (byte value : head) {
            if (value == 0) {
                throw new ValidationException(ErrorCode.FILE_INVALID);
            }
        }
        String prefix = new String(head).stripLeading().toLowerCase(Locale.ROOT);
        for (String forbidden : FORBIDDEN_TEXT_PREFIXES) {
            if (prefix.startsWith(forbidden)) {
                throw new ValidationException(ErrorCode.FILE_INVALID);
            }
        }
    }

    private byte[] readHead(Path target) throws IOException {
        try (InputStream input = Files.newInputStream(target)) {
            return input.readNBytes(512);
        }
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
