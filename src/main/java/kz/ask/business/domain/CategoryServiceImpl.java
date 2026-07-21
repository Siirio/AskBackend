package kz.ask.business.domain;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import kz.ask.business.api.dto.CategoryAutocompleteResponse;
import kz.ask.business.api.dto.CategoryAutocompleteResponse.CategorySuggestion;
import kz.ask.business.api.dto.CategoryResponse;
import kz.ask.business.domain.entity.Category;
import kz.ask.business.infrastructure.repository.CategoryRepository;
import kz.ask.catalog.infrastructure.repository.ProductRepository;
import kz.ask.service.infrastructure.repository.ServiceOfferingRepository;
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
    private final ProductRepository productRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;

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

    @Override
    public CategoryAutocompleteResponse autocomplete(String query, UUID businessId) {
        String q = (query == null || query.isBlank()) ? "" : query.trim();
        List<Category> standardCategories = q.isEmpty()
                ? categoryRepository.findByParentIsNullAndStatus(RecordStatus.ACTIVE)
                : categoryRepository.findByNameStartingWithIgnoreCaseAndStatus(q, RecordStatus.ACTIVE);

        List<CategorySuggestion> standard = standardCategories.stream()
                .limit(8)
                .map(c -> CategorySuggestion.builder()
                        .label(c.getName())
                        .categoryId(c.getId())
                        .build())
                .toList();

        List<CategorySuggestion> custom = new ArrayList<>();
        if (businessId != null && !q.isEmpty()) {
            Set<String> seen = new LinkedHashSet<>();
            List<String> productLabels = productRepository.findDistinctCategoryLabelsByBusiness(businessId, q);
            for (String label : productLabels) {
                if (seen.add(label.toLowerCase())) {
                    custom.add(CategorySuggestion.builder().label(label).categoryId(null).build());
                }
            }
            List<String> serviceLabels = serviceOfferingRepository.findDistinctCategoryLabelsByBusiness(businessId, q);
            for (String label : serviceLabels) {
                if (seen.add(label.toLowerCase())) {
                    custom.add(CategorySuggestion.builder().label(label).categoryId(null).build());
                }
            }
            if (custom.size() > 8) {
                custom = custom.subList(0, 8);
            }
        }

        return CategoryAutocompleteResponse.builder()
                .standard(standard)
                .custom(custom)
                .build();
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
