package kz.ask.shared.domain;

import java.util.List;
import java.util.UUID;
import kz.ask.shared.domain.dto.CityDto;
import kz.ask.shared.domain.entity.City;
import kz.ask.shared.infrastructure.repository.CityRepository;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CityServiceImpl implements CityService {

    private final CityRepository cityRepository;

    @Override
    public CityDto findById(UUID cityId) {
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CITY_NOT_FOUND));
        return toDto(city);
    }

    @Override
    public CityDto findByName(String name) {
        String trimmed = name == null ? "" : name.trim();
        City city = cityRepository.findByNameIgnoreCase(trimmed)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CITY_NOT_FOUND));
        return toDto(city);
    }

    @Override
    public List<CityDto> listAll() {
        return cityRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    private CityDto toDto(City entity) {
        return CityDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }
}
