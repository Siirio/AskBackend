package kz.ask.shared.domain;

import java.util.List;
import java.util.UUID;
import kz.ask.shared.domain.dto.CityDto;

public interface CityService {

    CityDto findById(UUID cityId);

    CityDto findByName(String name);

    List<CityDto> listAll();
}
