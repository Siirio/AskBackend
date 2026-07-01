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
                               Instant endDate, String type, String status, String coverUrl) {
        BrandDrop drop = businessMapper.toBrandDropEntity(
                businessRepository.getReferenceById(businessId),
                name,
                description,
                startDate,
                endDate,
                BrandDropType.valueOf(type),
                BrandDropStatus.valueOf(status),
                coverUrl);
        return businessMapper.toBrandDropDto(brandDropRepository.save(drop));
    }
}
