package kz.ask.business.category.api;

import jakarta.validation.Valid;
import kz.ask.business.category.api.dto.CategoryAutocompleteResponse;
import kz.ask.business.category.api.dto.CategoryCreateRequest;
import kz.ask.business.category.api.dto.CategoryResponse;
import kz.ask.business.category.domain.CategoryService;
import kz.ask.business.category.domain.enums.CategoryType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<CategoryAutocompleteResponse> autocomplete(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam CategoryType type) {
        return ResponseEntity.ok(categoryService.autocomplete(q, type));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.createUserCategory(request.getName(), request.getType()));
    }
}
