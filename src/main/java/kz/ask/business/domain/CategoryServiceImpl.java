package kz.ask.business.domain;

import java.util.List;
import java.util.UUID;
import kz.ask.business.api.dto.CategoryResponse;
import kz.ask.business.domain.entity.Category;
import kz.ask.business.infrastructure.repository.CategoryRepository;
import kz.ask.shared.domain.enums.RecordStatus;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private static final String GENERAL_CATEGORY_SLUG = "general";

    private final CategoryRepository categoryRepository;

    @Override
    public Category requireActiveCategory(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CATEGORY_NOT_FOUND));
        if (category.getStatus() != RecordStatus.ACTIVE) {
            throw new NotFoundException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        return category;
    }

    @Override
    public List<CategoryResponse> listRootCategories() {
        return categoryRepository.findByParentIsNullAndStatus(RecordStatus.ACTIVE)
                .stream()
                .map(this::toCategoryResponse)
                .toList();
    }

    @Override
    public List<CategoryResponse> listSubcategories(UUID parentId) {
        return categoryRepository.findByParentIdAndStatus(parentId, RecordStatus.ACTIVE)
                .stream()
                .map(this::toCategoryResponse)
                .toList();
    }

    @Override
    public UUID resolveServiceImportCategoryId(String preferredName) {
        if (preferredName != null && !preferredName.isBlank()) {
            var preferred = categoryRepository.findByNameIgnoreCase(preferredName.trim());
            if (preferred.isPresent() && preferred.get().getStatus() == RecordStatus.ACTIVE) {
                return preferred.get().getId();
            }
        }
        return categoryRepository.findByParentIsNullAndStatus(RecordStatus.ACTIVE)
                .stream()
                .filter(category -> GENERAL_CATEGORY_SLUG.equals(category.getSlug()))
                .findFirst()
                .or(() -> categoryRepository.findByParentIsNullAndStatus(RecordStatus.ACTIVE).stream().findFirst())
                .orElseThrow(() -> new NotFoundException(ErrorCode.CATEGORY_NOT_FOUND))
                .getId();
    }

    private CategoryResponse toCategoryResponse(Category category) {
        Category parent = category.getParent();
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .parentId(parent != null ? parent.getId() : null)
                .children(List.of())
                .build();
    }
}
