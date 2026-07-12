package kz.ask.business.domain;

import java.util.List;
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
        return createMember(businessId, userId, BusinessMemberRole.OWNER);
    }

    @Override
    @Transactional
    public BusinessMemberDto createMember(UUID businessId, UUID userId, BusinessMemberRole role) {
        Business business = businessRepository.getReferenceById(businessId);
        AppUser user = appUserRepository.getReferenceById(userId);
        BusinessMember entity = businessMemberRepository.save(
                businessMapper.toBusinessMemberEntity(business, user, role));
        return businessMapper.toBusinessMemberDto(entity);
    }

    @Override
    public BusinessMemberDto findByOwner(UUID userId) {
        BusinessMember member = businessMemberRepository.findByUserIdAndRoleAndStatus(
                userId, BusinessMemberRole.OWNER, RecordStatus.ACTIVE);
        return member != null ? businessMapper.toBusinessMemberDto(member) : null;
    }

    @Override
    public BusinessMemberDto findByUser(UUID userId) {
        List<BusinessMember> members = businessMemberRepository.findByUserIdAndStatus(
                userId, RecordStatus.ACTIVE);
        return members.isEmpty() ? null : businessMapper.toBusinessMemberDto(members.get(0));
    }

    @Override
    public BusinessMemberDto findByBusinessAndUser(UUID businessId, UUID userId) {
        BusinessMember member = businessMemberRepository.findByBusinessIdAndUserId(businessId, userId);
        return member != null ? businessMapper.toBusinessMemberDto(member) : null;
    }

    @Override
    public Boolean isOwnerOfBusiness(UUID businessId, UUID userId) {
        BusinessMember member = businessMemberRepository.findByUserIdAndRoleAndStatus(
                userId, BusinessMemberRole.OWNER, RecordStatus.ACTIVE);
        return member != null && member.getBusiness().getId().equals(businessId);
    }

    @Override
    public Boolean isManagerOrAboveOfBusiness(UUID businessId, UUID userId) {
        BusinessMemberDto member = findByBusinessAndUser(businessId, userId);
        if (member == null) {
            return false;
        }
        return member.getRole().equals(BusinessMemberRole.OWNER.name())
                || member.getRole().equals(BusinessMemberRole.MANAGER.name());
    }

    @Override
    public BusinessMemberRole getRoleInBusiness(UUID businessId, UUID userId) {
        BusinessMemberDto member = findByBusinessAndUser(businessId, userId);
        if (member == null) {
            return null;
        }
        try {
            return BusinessMemberRole.valueOf(member.getRole());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
