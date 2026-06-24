package kz.ask.business.domain;

import java.util.UUID;
import kz.ask.business.domain.dto.CityDto;
import kz.ask.business.domain.entity.City;
import kz.ask.business.infrastructure.mapper.BusinessMapper;
import kz.ask.business.infrastructure.repository.CityRepository;
import kz.ask.shared.domain.enums.RecordStatus;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CityServiceImpl implements CityService {

    private final CityRepository cityRepository;
    private final BusinessMapper businessMapper;

    @Override
    public CityDto findById(UUID cityId) {
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CITY_NOT_FOUND));
        return businessMapper.toCityDto(city);
    }

    @Override
    @Transactional
    public CityDto findOrCreateByName(String name) {
        String trimmed = name == null ? "" : name.trim();
        City city = cityRepository.findByNameIgnoreCase(trimmed)
                .orElseGet(() -> {
                    City created = new City();
                    created.setName(trimmed);
                    created.setCountryCode("KZ");
                    created.setStatus(RecordStatus.ACTIVE);
                    return cityRepository.save(created);
                });
        return businessMapper.toCityDto(city);
    }
}
