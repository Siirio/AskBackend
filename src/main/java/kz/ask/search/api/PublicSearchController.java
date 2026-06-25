package kz.ask.search.api;

import java.util.List;
import kz.ask.search.api.dto.SearchResultCardResponse;
import kz.ask.search.application.processor.PublicSearchProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class PublicSearchController {

    private final PublicSearchProcessor processor;

    @GetMapping
    public ResponseEntity<List<SearchResultCardResponse>> search(@RequestParam(required = false, name = "q") String query,
                                                                   @RequestParam(required = false, defaultValue = "all") String scope,
                                                                   @RequestParam(required = false) String category,
                                                                   @RequestParam(required = false) Integer page,
                                                                   @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(processor.search(query, scope, category, page, size));
    }
}
