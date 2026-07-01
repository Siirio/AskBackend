package kz.ask.business.domain;

import java.util.UUID;
import kz.ask.business.domain.dto.DataSourceDto;
import kz.ask.business.domain.enums.DataSourceType;

public interface DataSourceService {

    DataSourceDto resolve(UUID businessId, DataSourceType sourceType);
}
