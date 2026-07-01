package kz.ask.business.domain;

import java.util.UUID;
import kz.ask.business.domain.dto.BusinessMemberDto;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.entity.BusinessMember;
import kz.ask.business.domain.enums.BusinessMemberRole;
import kz.ask.business.infrastructure.mapper.BusinessMapper;
import kz.ask.business.infrastructure.repository.BusinessMemberRepository;
import kz.ask.business.infrastructure.repository.BusinessRepository;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.identity.infrastructure.repository.AppUserRepository;
import kz.ask.shared.domain.enums.RecordStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BusinessMemberServiceImpl implements BusinessMemberService {

    private final BusinessMemberRepository businessMemberRepository;
    private final BusinessRepository businessRepository;
    private final AppUserRepository appUserRepository;
    private final BusinessMapper businessMapper;

    @Override
    @Transactional
    public BusinessMemberDto createOwner(UUID businessId, UUID userId) {
        Business business = businessRepository.getReferenceById(businessId);
        AppUser user = appUserRepository.getReferenceById(userId);
        BusinessMember entity = businessMemberRepository.save(
                businessMapper.toBusinessMemberEntity(business, user));
        return businessMapper.toBusinessMemberDto(entity);
    }

    @Override
    public BusinessMemberDto findByOwner(UUID userId) {
        BusinessMember member = businessMemberRepository.findByUserIdAndRoleAndStatus(
                userId, BusinessMemberRole.OWNER, RecordStatus.ACTIVE);
        return member != null ? businessMapper.toBusinessMemberDto(member) : null;
    }

    @Override
    public Boolean isOwnerOfBusiness(UUID businessId, UUID userId) {
        BusinessMember member = businessMemberRepository.findByUserIdAndRoleAndStatus(
                userId, BusinessMemberRole.OWNER, RecordStatus.ACTIVE);
        return member != null && member.getBusiness().getId().equals(businessId);
    }
}
