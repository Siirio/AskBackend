package kz.ask.business.domain;

import java.util.UUID;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import kz.ask.business.domain.dto.BrandProfileDto;
import kz.ask.business.domain.entity.BrandProfile;
import kz.ask.business.infrastructure.mapper.BusinessMapper;
import kz.ask.business.infrastructure.repository.BrandProfileRepository;
import kz.ask.business.infrastructure.repository.BusinessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BrandProfileServiceImpl implements BrandProfileService {

    private final BrandProfileRepository brandProfileRepository;
    private final BusinessRepository businessRepository;
    private final BusinessMapper businessMapper;

    @Override
    @Transactional(readOnly = true)
    public BrandProfileDto findByBusinessId(UUID businessId) {
        return brandProfileRepository.findByBusinessId(businessId)
                .map(businessMapper::toBrandProfileDto)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, BrandProfileDto> findByBusinessIds(Collection<UUID> businessIds) {
        if (businessIds.isEmpty()) {
            return Map.of();
        }
        return brandProfileRepository.findByBusinessIdIn(businessIds).stream()
                .map(businessMapper::toBrandProfileDto)
                .collect(Collectors.toMap(BrandProfileDto::getBusinessId, Function.identity()));
    }

    @Override
    @Transactional
    public BrandProfileDto save(UUID businessId, String brandColor, String logoUrl, String coverUrl,
                                String toneOfVoice, String description, String instagramUrl,
                                String telegramUrl, String websiteUrl) {
        BrandProfile profile = brandProfileRepository.findByBusinessId(businessId)
                .orElseGet(() -> businessMapper.toBrandProfileEntity(businessRepository.getReferenceById(businessId)));
        businessMapper.updateBrandProfile(profile, brandColor, logoUrl, coverUrl, toneOfVoice,
                description, instagramUrl, telegramUrl, websiteUrl);
        return businessMapper.toBrandProfileDto(brandProfileRepository.save(profile));
    }
}
