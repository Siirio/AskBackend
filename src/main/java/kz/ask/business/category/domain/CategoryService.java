package kz.ask.business.category.domain;

import java.util.UUID;
import kz.ask.business.category.api.dto.CategoryAutocompleteResponse;
import kz.ask.business.category.api.dto.CategoryResponse;
import kz.ask.business.category.domain.entity.Category;
import kz.ask.business.category.domain.enums.CategoryType;

public interface CategoryService {

    Category requireActiveCategory(UUID categoryId, CategoryType type);

    Category resolveOrCreate(String name, CategoryType type);

    CategoryAutocompleteResponse autocomplete(String query, CategoryType type);

    CategoryResponse createUserCategory(String name, CategoryType type);
}
