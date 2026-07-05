package kz.ask.business.domain;

import java.util.List;
import java.util.UUID;
import kz.ask.business.domain.dto.BrandPageBlockDto;
import kz.ask.business.domain.entity.BrandPageBlock;
import kz.ask.business.domain.enums.BrandPageBlockType;
import kz.ask.business.domain.enums.StorefrontPageStatus;
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
        return brandPageBlockRepository.findByBusinessIdAndPageStatusAndEnabledOrderByDisplayOrderAsc(
                        businessId, StorefrontPageStatus.PUBLISHED, true)
                .stream()
                .map(businessMapper::toBrandPageBlockDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrandPageBlockDto> listDraft(UUID businessId) {
        return brandPageBlockRepository.findByBusinessIdAndPageStatusOrderByDisplayOrderAsc(
                        businessId, StorefrontPageStatus.DRAFT)
                .stream()
                .map(businessMapper::toBrandPageBlockDto)
                .toList();
    }

    @Override
    @Transactional
    public List<BrandPageBlockDto> replace(UUID businessId, List<BrandPageBlockDto> blocks) {
        return replaceByStatus(businessId, blocks, StorefrontPageStatus.PUBLISHED);
    }

    @Override
    @Transactional
    public List<BrandPageBlockDto> replaceDraft(UUID businessId, List<BrandPageBlockDto> blocks) {
        return replaceByStatus(businessId, blocks, StorefrontPageStatus.DRAFT);
    }

    @Override
    @Transactional
    public List<BrandPageBlockDto> publishDraft(UUID businessId) {
        List<BrandPageBlockDto> draftBlocks = listDraft(businessId);
        return replaceByStatus(businessId, draftBlocks, StorefrontPageStatus.PUBLISHED);
    }

    private List<BrandPageBlockDto> replaceByStatus(UUID businessId, List<BrandPageBlockDto> blocks,
                                                    StorefrontPageStatus pageStatus) {
        brandPageBlockRepository.deleteByBusinessIdAndPageStatus(businessId, pageStatus);
        List<BrandPageBlockDto> safeBlocks = blocks == null ? List.of() : blocks;
        List<BrandPageBlock> saved = safeBlocks.stream()
                .map(block -> businessMapper.toBrandPageBlockEntity(
                        businessRepository.getReferenceById(businessId),
                        BrandPageBlockType.valueOf(block.getBlockType()),
                        block.getDisplayOrder(),
                        block.getConfigJson(),
                        Boolean.TRUE.equals(block.getEnabled()),
                        pageStatus))
                .map(brandPageBlockRepository::save)
                .toList();
        return saved.stream().map(businessMapper::toBrandPageBlockDto).toList();
    }
}
