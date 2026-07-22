package kz.ask.business.member.domain;

import java.util.List;
import java.util.UUID;
import kz.ask.business.core.domain.entity.Business;
import kz.ask.business.member.domain.dto.BusinessMemberDto;
import kz.ask.business.member.domain.entity.BusinessMember;
import kz.ask.identity.authorization.domain.enums.Role;
import kz.ask.business.core.infrastructure.mapper.BusinessMapper;
import kz.ask.business.member.infrastructure.repository.BusinessMemberRepository;
import kz.ask.business.core.infrastructure.repository.BusinessRepository;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.identity.infrastructure.repository.AppUserRepository;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.NotFoundException;
import kz.ask.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessMemberServiceImpl implements BusinessMemberService {

    private final BusinessMemberRepository businessMemberRepository;
    private final BusinessRepository businessRepository;
    private final AppUserRepository appUserRepository;
    private final BusinessMapper businessMapper;

    @Override
    @Transactional
    public BusinessMemberDto createOwner(UUID businessId, UUID userId) {
        return createMember(businessId, userId, Role.OWNER);
    }

    @Override
    @Transactional
    public BusinessMemberDto createMember(UUID businessId, UUID userId, Role role) {
        if (role == null || role.getGroup() != kz.ask.identity.authorization.domain.enums.RoleGroup.BUSINESS) {
            throw new ValidationException(ErrorCode.BUSINESS_MEMBER_ROLE_NOT_ALLOWED);
        }
        Business business = businessRepository.getReferenceById(businessId);
        AppUser user = appUserRepository.getReferenceById(userId);
        BusinessMember entity = businessMemberRepository.save(
                businessMapper.toBusinessMemberEntity(business, user, role));
        return businessMapper.toBusinessMemberDto(entity);
    }

    @Override
    public BusinessMemberDto findByOwner(UUID userId) {
        List<BusinessMember> members = businessMemberRepository.findByUserIdAndRole(
                userId, Role.OWNER);
        return members.isEmpty() ? null : businessMapper.toBusinessMemberDto(members.get(0));
    }

    @Override
    public BusinessMemberDto findByUser(UUID userId) {
        List<BusinessMemberDto> members = findActiveByUser(userId);
        return members.isEmpty() ? null : members.get(0);
    }

    @Override
    public List<BusinessMemberDto> findActiveByUser(UUID userId) {
        return businessMemberRepository.findByUserId(userId).stream()
                .map(businessMapper::toBusinessMemberDto)
                .toList();
    }

    @Override
    public BusinessMemberDto findByBusinessAndUser(UUID businessId, UUID userId) {
        List<BusinessMember> members = businessMemberRepository.findByBusinessIdAndUserId(businessId, userId);
        return members.isEmpty() ? null : businessMapper.toBusinessMemberDto(members.get(0));
    }

    @Override
    public Boolean isOwnerOfBusiness(UUID businessId, UUID userId) {
        BusinessMemberDto member = findByBusinessAndUser(businessId, userId);
        return member != null
                && member.getRole().equals(Role.OWNER.name());
    }

    @Override
    public Boolean isManagerOrAboveOfBusiness(UUID businessId, UUID userId) {
        BusinessMemberDto member = findByBusinessAndUser(businessId, userId);
        if (member == null) {
            return false;
        }
        return member.getRole().equals(Role.OWNER.name())
                || member.getRole().equals(Role.MANAGER.name());
    }

    @Override
    public Role getRoleInBusiness(UUID businessId, UUID userId) {
        BusinessMemberDto member = findByBusinessAndUser(businessId, userId);
        if (member == null) {
            return null;
        }
        try {
            return Role.valueOf(member.getRole());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public List<BusinessMemberDto> findByBusiness(UUID businessId) {
        List<BusinessMember> members = businessMemberRepository.findByBusinessId(businessId);
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
    public BusinessMemberDto updateRole(UUID membershipId, Role role) {
        BusinessMember member = businessMemberRepository.findById(membershipId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.BUSINESS_MEMBER_NOT_FOUND));
        if (member.getRole() == Role.OWNER || role == Role.OWNER) {
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
        if (member.getRole() == Role.OWNER) {
            throw new ValidationException(ErrorCode.BUSINESS_MEMBER_ROLE_NOT_ALLOWED);
        }
        businessMemberRepository.delete(member);
        return businessMapper.toBusinessMemberDto(member);
    }
}
