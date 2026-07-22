package kz.ask.business.domain;

import java.util.List;
import java.util.UUID;
import kz.ask.business.api.dto.CategoryAutocompleteResponse;
import kz.ask.business.api.dto.CategoryResponse;
import kz.ask.business.domain.entity.Category;
import kz.ask.business.domain.enums.CategoryScope;

public interface CategoryService {

    Category requireActiveCategory(UUID categoryId);

    List<CategoryResponse> listRootCategories();

    List<CategoryResponse> listSubcategories(UUID parentId);

    UUID resolveServiceImportCategoryId(String preferredName);

    CategoryAutocompleteResponse autocomplete(String query, UUID businessId, CategoryScope scope);
}
