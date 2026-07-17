package kz.ask.chat.api;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import kz.ask.chat.api.dto.ChatFileDownloadDto;
import kz.ask.chat.api.dto.ChatFileUploadResponse;
import kz.ask.chat.application.ChatFileProcessor;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatFileController {

    private static final String CONTENT_TYPE_OPTIONS_HEADER = "X-Content-Type-Options";
    private static final String CONTENT_TYPE_OPTIONS_NOSNIFF = "nosniff";

    private final ChatFileProcessor chatFileProcessor;

    @PostMapping("/upload")
    public ResponseEntity<ChatFileUploadResponse> upload(@AuthenticationPrincipal AskPrincipal principal,
                                                         @RequestParam UUID conversationId,
                                                         @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(chatFileProcessor.upload(principal, conversationId, file));
    }

    @GetMapping("/files/{storedName}")
    public ResponseEntity<Resource> serve(@AuthenticationPrincipal AskPrincipal principal,
                                          @PathVariable String storedName) {
        ChatFileDownloadDto download = chatFileProcessor.download(principal, storedName);
        String downloadName = download.getOriginalName() != null ? download.getOriginalName() : storedName;
        return ResponseEntity.ok()
                .contentType(download.getContentType() != null
                        ? MediaType.parseMediaType(download.getContentType())
                        : MediaType.APPLICATION_OCTET_STREAM)
                .header(CONTENT_TYPE_OPTIONS_HEADER, CONTENT_TYPE_OPTIONS_NOSNIFF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(downloadName, StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(download.getResource());
    }
}
