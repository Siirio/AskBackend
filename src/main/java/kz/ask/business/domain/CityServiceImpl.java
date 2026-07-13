package kz.ask.business.domain;

import java.util.List;
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
    public CityDto findByName(String name) {
        String trimmed = name == null ? "" : name.trim();
        City city = cityRepository.findByNameIgnoreCase(trimmed)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CITY_NOT_FOUND));
        return businessMapper.toCityDto(city);
    }

    @Override
    public List<CityDto> listAll() {
        return cityRepository.findByStatusOrderByNameAsc(RecordStatus.ACTIVE)
                .stream()
                .map(businessMapper::toCityDto)
                .toList();
    }
}
