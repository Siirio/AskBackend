package kz.ask.search.basic.api;

import jakarta.validation.Valid;
import kz.ask.search.basic.api.dto.SearchV2Request;
import kz.ask.search.basic.api.dto.SearchV2Response;
import kz.ask.search.basic.application.processor.StructuredSearchProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class PublicSearchController {

    private final StructuredSearchProcessor processor;

    @PostMapping
    public ResponseEntity<SearchV2Response> search(@Valid @RequestBody SearchV2Request request) {
        return ResponseEntity.ok(processor.search(request));
    }
}
