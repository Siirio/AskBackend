package kz.ask.business.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import kz.ask.business.domain.dto.BrandDropDto;
import kz.ask.business.domain.entity.BrandDrop;
import kz.ask.business.domain.enums.BrandDropStatus;
import kz.ask.business.domain.enums.BrandDropType;
import kz.ask.business.infrastructure.mapper.BusinessMapper;
import kz.ask.business.infrastructure.repository.BrandDropRepository;
import kz.ask.business.infrastructure.repository.BusinessRepository;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BrandDropServiceImpl implements BrandDropService {

    private final BrandDropRepository brandDropRepository;
    private final BusinessRepository businessRepository;
    private final BusinessMapper businessMapper;

    @Override
    @Transactional(readOnly = true)
    public List<BrandDropDto> listPublic(UUID businessId) {
        return brandDropRepository.findByBusinessIdAndStatusInOrderByStartDateDesc(
                        businessId, List.of(BrandDropStatus.ACTIVE, BrandDropStatus.UPCOMING))
                .stream()
                .map(businessMapper::toBrandDropDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrandDropDto> listOwner(UUID businessId) {
        return brandDropRepository.findByBusinessIdOrderByStartDateDesc(businessId)
                .stream()
                .map(businessMapper::toBrandDropDto)
                .toList();
    }

    @Override
    @Transactional
    public BrandDropDto create(UUID businessId, String name, String description, Instant startDate,
                               Instant endDate, String type, String status, String coverUrl,
                               List<String> tags, List<UUID> productIds) {
        BrandDrop drop = businessMapper.toBrandDropEntity(
                businessRepository.getReferenceById(businessId),
                name,
                description,
                startDate,
                endDate,
                BrandDropType.valueOf(type),
                BrandDropStatus.valueOf(status),
                coverUrl,
                tags,
                productIds);
        return businessMapper.toBrandDropDto(brandDropRepository.save(drop));
    }

    @Override
    @Transactional
    public BrandDropDto update(UUID businessId, UUID dropId, String name, String description, Instant startDate,
                               Instant endDate, String type, String status, String coverUrl,
                               List<String> tags, List<UUID> productIds) {
        BrandDrop drop = requireDrop(businessId, dropId);
        if (name != null) {
            drop.setName(name);
        }
        if (description != null) {
            drop.setDescription(description);
        }
        if (startDate != null) {
            drop.setStartDate(startDate);
        }
        if (endDate != null) {
            drop.setEndDate(endDate);
        }
        if (type != null) {
            drop.setType(BrandDropType.valueOf(type));
        }
        if (status != null) {
            drop.setStatus(BrandDropStatus.valueOf(status));
        }
        if (coverUrl != null) {
            drop.setCoverUrl(coverUrl);
        }
        if (tags != null) {
            drop.setTags(tags);
        }
        if (productIds != null) {
            drop.setProductIds(productIds);
        }
        return businessMapper.toBrandDropDto(brandDropRepository.save(drop));
    }

    @Override
    @Transactional
    public BrandDropDto cancel(UUID businessId, UUID dropId) {
        BrandDrop drop = requireDrop(businessId, dropId);
        drop.setStatus(BrandDropStatus.CANCELLED);
        return businessMapper.toBrandDropDto(brandDropRepository.save(drop));
    }

    @Override
    @Transactional
    public void delete(UUID businessId, UUID dropId) {
        brandDropRepository.delete(requireDrop(businessId, dropId));
    }

    private BrandDrop requireDrop(UUID businessId, UUID dropId) {
        return brandDropRepository.findByIdAndBusinessId(dropId, businessId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.DROP_NOT_FOUND));
    }
}
