package kz.ask.business.domain;

import java.util.UUID;
import kz.ask.business.domain.dto.DataSourceDto;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.entity.DataSource;
import kz.ask.business.domain.enums.DataSourceType;
import kz.ask.business.infrastructure.mapper.BusinessMapper;
import kz.ask.business.infrastructure.repository.BusinessRepository;
import kz.ask.business.infrastructure.repository.DataSourceRepository;
import kz.ask.shared.domain.enums.RecordStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DataSourceServiceImpl implements DataSourceService {

    private final DataSourceRepository dataSourceRepository;
    private final BusinessRepository businessRepository;
    private final BusinessMapper businessMapper;

    @Override
    @Transactional
    public DataSourceDto resolve(UUID businessId, DataSourceType sourceType) {
        Business business = businessRepository.getReferenceById(businessId);
        DataSource entity = dataSourceRepository.findByBusinessIdAndSourceType(businessId, sourceType)
            .orElseGet(() -> {
                DataSource ds = new DataSource();
                ds.setBusiness(business);
                ds.setSourceType(sourceType);
                ds.setName(sourceType.name());
                ds.setStatus(RecordStatus.ACTIVE);
                return dataSourceRepository.save(ds);
            });
        return businessMapper.toDataSourceDto(entity);
    }
}
