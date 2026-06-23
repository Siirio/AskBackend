package kz.ask.business.domain;

import java.util.UUID;
import kz.ask.business.domain.entity.Category;

public interface CategoryService {

    Category requireActiveCategory(UUID categoryId);
}
