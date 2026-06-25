package kz.ask.business.domain;

import java.util.List;
import java.util.UUID;
import kz.ask.business.domain.dto.CityDto;

public interface CityService {

    CityDto findById(UUID cityId);

    CityDto findOrCreateByName(String name);

    List<CityDto> listAll();
}
