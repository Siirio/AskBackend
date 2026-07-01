package kz.ask.business.domain;

import java.util.List;
import java.util.UUID;
import kz.ask.business.domain.dto.BrandPageBlockDto;
import kz.ask.business.domain.entity.BrandPageBlock;
import kz.ask.business.domain.enums.BrandPageBlockType;
import kz.ask.business.infrastructure.mapper.BusinessMapper;
import kz.ask.business.infrastructure.repository.BrandPageBlockRepository;
import kz.ask.business.infrastructure.repository.BusinessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BrandPageBlockServiceImpl implements BrandPageBlockService {

    private final BrandPageBlockRepository brandPageBlockRepository;
    private final BusinessRepository businessRepository;
    private final BusinessMapper businessMapper;

    @Override
    @Transactional(readOnly = true)
    public List<BrandPageBlockDto> listPublic(UUID businessId) {
        return brandPageBlockRepository.findByBusinessIdAndEnabledOrderByDisplayOrderAsc(businessId, true)
                .stream()
                .map(businessMapper::toBrandPageBlockDto)
                .toList();
    }

    @Override
    @Transactional
    public List<BrandPageBlockDto> replace(UUID businessId, List<BrandPageBlockDto> blocks) {
        brandPageBlockRepository.deleteByBusinessId(businessId);
        List<BrandPageBlock> saved = blocks.stream()
                .map(block -> businessMapper.toBrandPageBlockEntity(
                        businessRepository.getReferenceById(businessId),
                        BrandPageBlockType.valueOf(block.getBlockType()),
                        block.getDisplayOrder(),
                        block.getConfigJson(),
                        Boolean.TRUE.equals(block.getEnabled())))
                .map(brandPageBlockRepository::save)
                .toList();
        return saved.stream().map(businessMapper::toBrandPageBlockDto).toList();
    }
}
