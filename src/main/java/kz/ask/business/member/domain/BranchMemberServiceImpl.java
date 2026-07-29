package kz.ask.business.member.domain;

import java.util.List;
import java.util.UUID;
import kz.ask.business.member.domain.dto.BranchMemberDto;
import kz.ask.business.member.domain.entity.BranchMember;
import kz.ask.business.branch.domain.entity.BusinessBranch;
import kz.ask.identity.authorization.domain.enums.Role;
import kz.ask.identity.authorization.domain.enums.RoleGroup;
import kz.ask.business.core.infrastructure.mapper.BusinessMapper;
import kz.ask.business.member.infrastructure.repository.BranchMemberRepository;
import kz.ask.business.branch.infrastructure.repository.BusinessBranchRepository;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.identity.infrastructure.repository.AppUserRepository;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BranchMemberServiceImpl implements BranchMemberService {

    private final BranchMemberRepository branchMemberRepository;
    private final BusinessBranchRepository businessBranchRepository;
    private final AppUserRepository appUserRepository;
    private final BusinessMapper businessMapper;

    @Override
    @Transactional
    public BranchMemberDto addMember(UUID branchId, UUID userId, Role role) {
        validateRole(role);
        BusinessBranch branch = businessBranchRepository.getReferenceById(branchId);
        AppUser user = appUserRepository.getReferenceById(userId);
        BranchMember entity = branchMemberRepository.save(businessMapper.toBranchMemberEntity(branch, user, role));
        return businessMapper.toBranchMemberDto(entity);
    }

    @Override
    @Transactional
    public BranchMemberDto updateMemberRole(UUID memberId, Role role) {
        validateRole(role);
        BranchMember member = branchMemberRepository.findById(memberId)
                .orElseThrow();
        member.setRole(role);
        return businessMapper.toBranchMemberDto(member);
    }

    private void validateRole(Role role) {
        if (role == null || role.getGroup() != RoleGroup.BUSINESS
                || role == Role.OWNER || role == Role.CUSTOMER) {
            throw new ValidationException(ErrorCode.BUSINESS_MEMBER_ROLE_NOT_ALLOWED);
        }
    }

    @Override
    public List<BranchMemberDto> findByBranch(UUID branchId) {
        List<BranchMember> entities = branchMemberRepository.findByBranchId(branchId);
        return businessMapper.toBranchMemberDtoList(entities);
    }

    @Override
    public Boolean isStaffOfBranch(UUID branchId, UUID userId) {
        List<BranchMember> members = branchMemberRepository.findByUserId(userId);
        return members.stream().anyMatch(m -> m.getBranch().getId().equals(branchId));
    }

    @Override
    public Boolean isBranchStaff(UUID userId) {
        List<BranchMember> members = branchMemberRepository.findByUserId(userId);
        return !members.isEmpty();
    }

    @Override
    public BranchMemberDto findByUser(UUID userId) {
        List<BranchMember> members = branchMemberRepository.findByUserId(userId);
        return members.isEmpty() ? null : businessMapper.toBranchMemberDto(members.get(0));
    }

    @Override
    @Transactional
    public void removeByUser(UUID userId) {
        branchMemberRepository.deleteAll(branchMemberRepository.findByUserId(userId));
    }
}
