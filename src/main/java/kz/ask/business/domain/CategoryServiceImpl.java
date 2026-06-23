package kz.ask.business.domain;

import java.util.UUID;
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
}
