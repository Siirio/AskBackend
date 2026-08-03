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

    private static final java.util.regex.Pattern RUSSIAN_CITY_PREFIX =
            java.util.regex.Pattern.compile("(?iu)^\\s*г(?:ород)?\\.?\\s+(.+)$");
    private static final java.util.regex.Pattern KAZAKH_CITY_SUFFIX =
            java.util.regex.Pattern.compile("(?iu)^\\s*(.+?)\\s+(?:қ\\.?|қаласы)\\s*$");

    private final CityRepository cityRepository;

    @Override
    public CityDto findById(UUID cityId) {
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CITY_NOT_FOUND));
        return toDto(city);
    }

    @Override
    public CityDto findByName(String name) {
        City city = cityRepository.findByNameIgnoreCase(canonicalCityName(name))
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

    private String canonicalCityName(String value) {
        String trimmed = value == null ? "" : value.trim();
        java.util.regex.Matcher russian = RUSSIAN_CITY_PREFIX.matcher(trimmed);
        if (russian.matches()) {
            return russian.group(1).trim();
        }
        java.util.regex.Matcher kazakh = KAZAKH_CITY_SUFFIX.matcher(trimmed);
        if (kazakh.matches()) {
            return kazakh.group(1).trim();
        }
        return trimmed;
    }

    private CityDto toDto(City entity) {
        return CityDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }
}
