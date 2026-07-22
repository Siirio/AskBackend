package kz.ask.business.category.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import kz.ask.business.category.api.dto.CategoryAutocompleteResponse;
import kz.ask.business.category.api.dto.CategoryResponse;
import kz.ask.business.category.api.dto.CategorySuggestionResponse;
import kz.ask.business.category.domain.entity.Category;
import kz.ask.business.category.domain.enums.CategorySource;
import kz.ask.business.category.domain.enums.CategoryType;
import kz.ask.business.category.infrastructure.repository.CategoryRepository;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.NotFoundException;
import kz.ask.shared.error.ValidationException;
import kz.ask.shared.infrastructure.uuid.UuidV7;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public Category requireActiveCategory(UUID categoryId, CategoryType type) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CATEGORY_NOT_FOUND));
        if (category.getType() != type) {
            throw new ValidationException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        return category;
    }

    @Override
    @Transactional
    public Category resolveOrCreate(String name, CategoryType type) {
        if (name == null || name.isBlank()) {
            throw new ValidationException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        String normalized = name.trim();
        Category existing = categoryRepository.findByNameIgnoreCaseAndType(normalized, type).orElse(null);
        if (existing != null) {
            return existing;
        }
        categoryRepository.insertIfAbsent(
                UuidV7.create(), Instant.now(), normalized,
                normalized.toLowerCase().replace(' ', '-'), type.name(), CategorySource.USER.name());
        return categoryRepository.findByNameIgnoreCaseAndType(normalized, type)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryAutocompleteResponse autocomplete(String query, CategoryType type) {
        String normalized = query == null ? "" : query.trim();
        List<Category> categories = normalized.isBlank()
                ? categoryRepository.findByTypeOrderByName(type)
                : categoryRepository.findByTypeAndNameStartingWithIgnoreCaseOrderByName(type, normalized);
        return CategoryAutocompleteResponse.builder()
                .suggestions(categories.stream().map(this::toSuggestion).toList())
                .build();
    }

    @Override
    @Transactional
    public CategoryResponse createUserCategory(String name, CategoryType type) {
        return toResponse(resolveOrCreate(name, type));
    }

    private CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .type(category.getType().name())
                .source(category.getSource().name())
                .build();
    }

    private CategorySuggestionResponse toSuggestion(Category category) {
        return CategorySuggestionResponse.builder()
                .categoryId(category.getId())
                .label(category.getName())
                .type(category.getType().name())
                .source(category.getSource().name())
                .build();
    }
}
