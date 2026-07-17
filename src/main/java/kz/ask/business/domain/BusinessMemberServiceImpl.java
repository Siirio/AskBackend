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
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.NotFoundException;
import kz.ask.shared.error.ValidationException;
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
        List<BusinessMemberDto> members = findActiveByUser(userId);
        return members.isEmpty() ? null : members.get(0);
    }

    @Override
    public List<BusinessMemberDto> findActiveByUser(UUID userId) {
        return businessMemberRepository.findByUserIdAndStatus(userId, RecordStatus.ACTIVE).stream()
                .map(businessMapper::toBusinessMemberDto)
                .toList();
    }

    @Override
    public BusinessMemberDto findByBusinessAndUser(UUID businessId, UUID userId) {
        BusinessMember member = businessMemberRepository.findByBusinessIdAndUserId(businessId, userId);
        return member != null ? businessMapper.toBusinessMemberDto(member) : null;
    }

    @Override
    public Boolean isOwnerOfBusiness(UUID businessId, UUID userId) {
        BusinessMemberDto member = findByBusinessAndUser(businessId, userId);
        return member != null
                && member.getStatus().equals(RecordStatus.ACTIVE.name())
                && member.getRole().equals(BusinessMemberRole.OWNER.name());
    }

    @Override
    public Boolean isManagerOrAboveOfBusiness(UUID businessId, UUID userId) {
        BusinessMemberDto member = findByBusinessAndUser(businessId, userId);
        if (member == null) {
            return false;
        }
        return member.getStatus().equals(RecordStatus.ACTIVE.name())
                && (member.getRole().equals(BusinessMemberRole.OWNER.name())
                || member.getRole().equals(BusinessMemberRole.MANAGER.name()));
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

    @Override
    public List<BusinessMemberDto> findByBusiness(UUID businessId) {
        List<BusinessMember> members = businessMemberRepository.findByBusinessIdAndStatus(
                businessId, RecordStatus.ACTIVE);
        return members.stream().map(businessMapper::toBusinessMemberDto).toList();
    }

    @Override
    public BusinessMemberDto findById(UUID membershipId) {
        return businessMemberRepository.findById(membershipId)
                .map(businessMapper::toBusinessMemberDto)
                .orElseThrow(() -> new NotFoundException(ErrorCode.BUSINESS_MEMBER_NOT_FOUND));
    }

    @Override
    @Transactional
    public BusinessMemberDto updateRole(UUID membershipId, BusinessMemberRole role) {
        BusinessMember member = businessMemberRepository.findById(membershipId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.BUSINESS_MEMBER_NOT_FOUND));
        if (member.getRole() == BusinessMemberRole.OWNER || role == BusinessMemberRole.OWNER) {
            throw new ValidationException(ErrorCode.BUSINESS_MEMBER_ROLE_NOT_ALLOWED);
        }
        member.setRole(role);
        return businessMapper.toBusinessMemberDto(businessMemberRepository.save(member));
    }

    @Override
    @Transactional
    public BusinessMemberDto deactivate(UUID membershipId) {
        BusinessMember member = businessMemberRepository.findById(membershipId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.BUSINESS_MEMBER_NOT_FOUND));
        if (member.getRole() == BusinessMemberRole.OWNER) {
            throw new ValidationException(ErrorCode.BUSINESS_MEMBER_ROLE_NOT_ALLOWED);
        }
        member.setStatus(RecordStatus.INACTIVE);
        return businessMapper.toBusinessMemberDto(businessMemberRepository.save(member));
    }
}
