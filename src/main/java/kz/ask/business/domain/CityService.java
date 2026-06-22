package kz.ask.business.domain;

import java.util.UUID;
import kz.ask.business.domain.dto.CityDto;

public interface CityService {

    CityDto findById(UUID cityId);
}
