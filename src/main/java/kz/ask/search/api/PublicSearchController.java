package kz.ask.search.api;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import java.util.List;
import kz.ask.search.api.dto.SearchIntentStructureRequest;
import kz.ask.search.api.dto.SearchResultCardResponse;
import kz.ask.search.api.dto.SearchV2Request;
import kz.ask.search.api.dto.SearchV2Response;
import kz.ask.search.api.dto.StructuredSearchResponse;
import kz.ask.search.application.processor.PublicSearchProcessor;
import kz.ask.search.application.processor.SearchV2Processor;
import kz.ask.search.application.processor.StructuredSearchProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class PublicSearchController {

    private final PublicSearchProcessor processor;
    private final StructuredSearchProcessor structuredSearchProcessor;
    private final SearchV2Processor searchV2Processor;

    @GetMapping
    public ResponseEntity<List<SearchResultCardResponse>> search(@RequestParam(required = false, name = "q") String query,
                                                                   @RequestParam(required = false, defaultValue = "all") String scope,
                                                                   @RequestParam(required = false) String category,
                                                                   @RequestParam(required = false) Integer page,
                                                                   @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(processor.search(query, scope, category, page, size));
    }

    @PostMapping("/intent-structure")
    public ResponseEntity<JsonNode> structure(@Valid @RequestBody SearchIntentStructureRequest request) {
        return ResponseEntity.ok(structuredSearchProcessor.structure(request));
    }

    @PostMapping
    public ResponseEntity<StructuredSearchResponse> structuredSearch(@Valid @RequestBody SearchIntentStructureRequest request,
                                                                     @RequestParam(required = false) Integer page,
                                                                     @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(structuredSearchProcessor.search(request, page, size));
    }

    @PostMapping("/v2")
    public ResponseEntity<SearchV2Response> searchV2(@Valid @RequestBody SearchV2Request request) {
        return ResponseEntity.ok(searchV2Processor.search(request));
    }
}
