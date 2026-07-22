package kz.ask.business.profile.domain;

import java.util.UUID;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import kz.ask.business.profile.domain.dto.BusinessProfileDto;
import kz.ask.business.profile.domain.entity.BusinessProfile;
import kz.ask.business.core.infrastructure.mapper.BusinessMapper;
import kz.ask.business.core.infrastructure.repository.BusinessRepository;
import kz.ask.business.profile.infrastructure.repository.BusinessProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BusinessProfileServiceImpl implements BusinessProfileService {

    private final BusinessProfileRepository businessProfileRepository;
    private final BusinessRepository businessRepository;
    private final BusinessMapper businessMapper;

    @Override
    @Transactional(readOnly = true)
    public BusinessProfileDto findByBusinessId(UUID businessId) {
        return businessProfileRepository.findByBusinessId(businessId)
                .map(businessMapper::toBusinessProfileDto)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, BusinessProfileDto> findByBusinessIds(Collection<UUID> businessIds) {
        if (businessIds.isEmpty()) {
            return Map.of();
        }
        return businessProfileRepository.findByBusinessIdIn(businessIds).stream()
                .map(businessMapper::toBusinessProfileDto)
                .collect(Collectors.toMap(BusinessProfileDto::getBusinessId, Function.identity()));
    }

    @Override
    @Transactional
    public BusinessProfileDto save(UUID businessId, String brandColor, String logoUrl, String coverUrl,
                                    String description, String number, String email,
                                    String instagramUrl, String telegramUrl, String websiteUrl) {
        BusinessProfile profile = businessProfileRepository.findByBusinessId(businessId)
                .orElseGet(() -> businessMapper.toBusinessProfileEntity(businessRepository.getReferenceById(businessId)));
        businessMapper.updateBusinessProfile(profile, brandColor, logoUrl, coverUrl,
                description, number, email, instagramUrl, telegramUrl, websiteUrl);
        return businessMapper.toBusinessProfileDto(businessProfileRepository.save(profile));
    }
}
