package kz.ask.business.media.api;

import kz.ask.business.media.application.BusinessMediaProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/business-media")
@RequiredArgsConstructor
public class BusinessMediaController {

    private static final String CONTENT_TYPE_OPTIONS_HEADER = "X-Content-Type-Options";
    private static final String CONTENT_TYPE_OPTIONS_NOSNIFF = "nosniff";

    private final BusinessMediaProcessor businessMediaProcessor;

    @GetMapping("/files/{storedName}")
    public ResponseEntity<Resource> serve(@PathVariable String storedName) {
        Resource resource = businessMediaProcessor.resolve(storedName);
        MediaType contentType = MediaTypeFactory.getMediaType(resource)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok()
                .contentType(contentType)
                .header(CONTENT_TYPE_OPTIONS_HEADER, CONTENT_TYPE_OPTIONS_NOSNIFF)
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000, immutable")
                .body(resource);
    }
}
