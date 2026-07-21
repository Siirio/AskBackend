package kz.ask.business.api;

import java.util.List;
import java.util.UUID;
import kz.ask.business.api.dto.CategoryAutocompleteResponse;
import kz.ask.business.api.dto.CategoryResponse;
import kz.ask.business.domain.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> listRoots() {
        return ResponseEntity.ok(categoryService.listRootCategories());
    }

    @GetMapping("/{parentId}/subcategories")
    public ResponseEntity<List<CategoryResponse>> listSubcategories(@PathVariable UUID parentId) {
        return ResponseEntity.ok(categoryService.listSubcategories(parentId));
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<CategoryAutocompleteResponse> autocomplete(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(required = false) UUID businessId) {
        return ResponseEntity.ok(categoryService.autocomplete(q, businessId));
    }
}
