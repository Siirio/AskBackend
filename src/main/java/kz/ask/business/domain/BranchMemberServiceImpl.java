package kz.ask.business.domain;

import java.util.List;
import java.util.UUID;
import kz.ask.business.domain.dto.BranchMemberDto;
import kz.ask.business.domain.entity.BranchMember;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.business.domain.enums.BranchMemberRole;
import kz.ask.business.infrastructure.mapper.BusinessMapper;
import kz.ask.business.infrastructure.repository.BranchMemberRepository;
import kz.ask.business.infrastructure.repository.BusinessBranchRepository;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.identity.infrastructure.repository.AppUserRepository;
import kz.ask.shared.domain.enums.RecordStatus;
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
    public BranchMemberDto addMember(UUID branchId, UUID userId, BranchMemberRole role) {
        BusinessBranch branch = businessBranchRepository.getReferenceById(branchId);
        AppUser user = appUserRepository.getReferenceById(userId);
        BranchMember entity = branchMemberRepository.save(businessMapper.toBranchMemberEntity(branch, user, role));
        return businessMapper.toBranchMemberDto(entity);
    }

    @Override
    @Transactional
    public BranchMemberDto updateMemberRole(UUID memberId, BranchMemberRole role) {
        BranchMember member = branchMemberRepository.findById(memberId)
                .orElseThrow();
        member.setRole(role);
        return businessMapper.toBranchMemberDto(member);
    }

    @Override
    public List<BranchMemberDto> findByBranch(UUID branchId) {
        List<BranchMember> entities = branchMemberRepository.findByBranchIdAndStatus(branchId, RecordStatus.ACTIVE);
        return businessMapper.toBranchMemberDtoList(entities);
    }

    @Override
    public Boolean isStaffOfBranch(UUID branchId, UUID userId) {
        List<BranchMember> members = branchMemberRepository.findByUserIdAndStatus(userId, RecordStatus.ACTIVE);
        return members.stream().anyMatch(m -> m.getBranch().getId().equals(branchId));
    }

    @Override
    public Boolean isBranchStaff(UUID userId) {
        List<BranchMember> members = branchMemberRepository.findByUserIdAndStatus(userId, RecordStatus.ACTIVE);
        return !members.isEmpty();
    }

    @Override
    public BranchMemberDto findByUser(UUID userId) {
        List<BranchMember> members = branchMemberRepository.findByUserIdAndStatus(userId, RecordStatus.ACTIVE);
        return members.isEmpty() ? null : businessMapper.toBranchMemberDto(members.get(0));
    }
}
