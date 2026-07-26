package kz.ask.offer.item.domain;

import java.util.List;
import java.util.UUID;
import kz.ask.offer.item.infrastructure.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public Boolean allBelongToBusiness(UUID businessId, List<UUID> itemIds) {
        return itemIds.isEmpty()
                || productRepository.countByIdInAndBusinessId(itemIds, businessId) == itemIds.size();
    }
}
