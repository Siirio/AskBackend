package kz.ask.business.infrastructure.repository;

import java.util.List;
import java.util.UUID;
import kz.ask.business.domain.entity.BusinessMember;
import kz.ask.business.domain.enums.BusinessMemberRole;
import kz.ask.shared.domain.enums.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessMemberRepository extends JpaRepository<BusinessMember, UUID> {

    BusinessMember findByUserIdAndRoleAndStatus(UUID userId, BusinessMemberRole role, RecordStatus status);

    List<BusinessMember> findByBusinessIdAndRoleAndStatus(UUID businessId, BusinessMemberRole role, RecordStatus status);

    List<BusinessMember> findByUserIdAndStatus(UUID userId, RecordStatus status);

    BusinessMember findByBusinessIdAndUserId(UUID businessId, UUID userId);
}
