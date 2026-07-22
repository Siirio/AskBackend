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
import kz.ask.business.domain.enums.CategoryScope;
import kz.ask.business.infrastructure.repository.CategoryRepository;
import kz.ask.offer.item.infrastructure.repository.ProductRepository;
import kz.ask.offer.service.infrastructure.repository.ServiceOfferingRepository;
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
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    @Override
    public List<CategoryResponse> listRootCategories() {
        return categoryRepository.findByParentIsNull()
                .stream()
                .map(this::toCategoryResponse)
                .toList();
    }

    @Override
    public List<CategoryResponse> listSubcategories(UUID parentId) {
        return categoryRepository.findByParentId(parentId)
                .stream()
                .map(this::toCategoryResponse)
                .toList();
    }

    @Override
    public UUID resolveServiceImportCategoryId(String preferredName) {
        if (preferredName != null && !preferredName.isBlank()) {
            var preferred = categoryRepository.findByNameIgnoreCase(preferredName.trim());
            if (preferred.isPresent()) {
                return preferred.get().getId();
            }
        }
        return categoryRepository.findByParentIsNull()
                .stream()
                .filter(category -> GENERAL_CATEGORY_SLUG.equals(category.getSlug()))
                .findFirst()
                .or(() -> categoryRepository.findByParentIsNull().stream().findFirst())
                .orElseThrow(() -> new NotFoundException(ErrorCode.CATEGORY_NOT_FOUND))
                .getId();
    }

    @Override
    public CategoryAutocompleteResponse autocomplete(String query, UUID businessId, CategoryScope scope) {
        String q = (query == null || query.isBlank()) ? "" : query.trim();
        List<Category> matchingCategories;

        if (q.isEmpty()) {
            matchingCategories = categoryRepository.findRootsByScope(scope != null ? scope : CategoryScope.BOTH);
        } else {
            matchingCategories = categoryRepository.findByNameStartingWithScope(q, scope != null ? scope : CategoryScope.BOTH);

            List<Category> aliasMatches = categoryRepository.findByAliasStartingWithAndScope(q, scope != null ? scope : CategoryScope.BOTH);
            for (Category aliasMatch : aliasMatches) {
                if (matchingCategories.stream().noneMatch(c -> c.getId().equals(aliasMatch.getId()))) {
                    matchingCategories.add(aliasMatch);
                }
            }
        }

        List<CategorySuggestion> standard = matchingCategories.stream()
                .limit(8)
                .map(this::toSuggestion)
                .toList();

        List<CategorySuggestion> custom = new ArrayList<>();
        if (businessId != null && !q.isEmpty()) {
            Set<String> seen = new LinkedHashSet<>();
            List<String> productLabels = productRepository.findDistinctCategoryLabelsByBusiness(businessId, q);
            for (String label : productLabels) {
                if (seen.add(label.toLowerCase())) {
                    custom.add(CategorySuggestion.builder().label(label).categoryId(null).custom(true).build());
                }
            }
            List<String> serviceLabels = serviceOfferingRepository.findDistinctCategoryLabelsByBusiness(businessId, q);
            for (String label : serviceLabels) {
                if (seen.add(label.toLowerCase())) {
                    custom.add(CategorySuggestion.builder().label(label).categoryId(null).custom(true).build());
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

    private CategorySuggestion toSuggestion(Category category) {
        Category parent = category.getParent();
        String parentLabel = parent != null ? parent.getName() : null;
        String path = parent != null ? parent.getName() + " -> " + category.getName() : category.getName();
        return CategorySuggestion.builder()
                .label(category.getName())
                .categoryId(category.getId())
                .parentId(parent != null ? parent.getId() : null)
                .parentLabel(parentLabel)
                .path(path)
                .custom(false)
                .build();
    }

    private CategoryResponse toCategoryResponse(Category category) {
        Category parent = category.getParent();
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .parentId(parent != null ? parent.getId() : null)
                .scope(category.getScope().name())
                .children(List.of())
                .build();
    }
}
