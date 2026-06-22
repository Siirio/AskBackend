package kz.ask.catalog.domain.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import kz.ask.catalog.domain.dto.CatalogImportColumnMappingDto;
import kz.ask.catalog.domain.entity.CatalogImport;
import kz.ask.catalog.domain.entity.CatalogImportColumnMapping;
import kz.ask.catalog.infrastructure.mapper.CatalogImportMapper;
import kz.ask.catalog.infrastructure.repository.CatalogImportColumnMappingRepository;
import kz.ask.catalog.infrastructure.repository.CatalogImportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CatalogImportColumnMappingServiceImpl implements CatalogImportColumnMappingService {

    private final CatalogImportColumnMappingRepository mappingRepository;
    private final CatalogImportRepository catalogImportRepository;
    private final CatalogImportMapper mapper;

    @Override
    @Transactional
    public void saveAll(UUID catalogImportId, List<CatalogImportColumnMappingDto> dtos) {
        deleteByCatalogImportId(catalogImportId);
        CatalogImport catalogImport = catalogImportRepository.getReferenceById(catalogImportId);
        List<CatalogImportColumnMapping> entities = dtos.stream()
                .map(dto -> mapper.toColumnMappingEntity(dto, catalogImport))
                .collect(Collectors.toList());
        mappingRepository.saveAll(entities);
    }

    @Override
    public List<CatalogImportColumnMappingDto> findByCatalogImportId(UUID catalogImportId) {
        List<CatalogImportColumnMapping> entities = mappingRepository.findByCatalogImportId(catalogImportId);
        return mapper.toColumnMappingDtoList(entities);
    }

    @Override
    @Transactional
    public void deleteByCatalogImportId(UUID catalogImportId) {
        mappingRepository.deleteByCatalogImportId(catalogImportId);
    }
}
