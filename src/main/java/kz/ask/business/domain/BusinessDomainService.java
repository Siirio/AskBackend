package kz.ask.business.domain;

import java.util.UUID;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.business.domain.entity.BusinessContact;
import kz.ask.business.domain.entity.BusinessMember;
import kz.ask.business.domain.entity.City;
import kz.ask.business.domain.enums.BusinessMemberRole;
import kz.ask.business.domain.enums.ContactType;
import kz.ask.business.infrastructure.repository.BusinessBranchRepository;
import kz.ask.business.infrastructure.repository.BusinessContactRepository;
import kz.ask.business.infrastructure.repository.BusinessMemberRepository;
import kz.ask.business.infrastructure.repository.BusinessRepository;
import kz.ask.business.infrastructure.repository.CityRepository;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.shared.domain.enums.RecordStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BusinessDomainService {

    private final BusinessRepository businessRepository;
    private final BusinessBranchRepository businessBranchRepository;
    private final BusinessMemberRepository businessMemberRepository;
    private final BusinessContactRepository businessContactRepository;
    private final CityRepository cityRepository;

    public BusinessDomainService(BusinessRepository businessRepository,
                                  BusinessBranchRepository businessBranchRepository,
                                  BusinessMemberRepository businessMemberRepository,
                                  BusinessContactRepository businessContactRepository,
                                  CityRepository cityRepository) {
        this.businessRepository = businessRepository;
        this.businessBranchRepository = businessBranchRepository;
        this.businessMemberRepository = businessMemberRepository;
        this.businessContactRepository = businessContactRepository;
        this.cityRepository = cityRepository;
    }

    @Transactional
    public BusinessRegistrationResult registerBusiness(AppUser owner,
                                                        String businessName,
                                                        String branchName,
                                                        UUID branchCityId,
                                                        String branchAddress,
                                                        boolean onlineOnly,
                                                        String contactEmail,
                                                        String contactPhone) {
        Business business = new Business();
        business.setName(businessName);
        business.setStatus(RecordStatus.ACTIVE);
        business = businessRepository.save(business);

        City city = null;
        if (branchCityId != null) {
            city = cityRepository.findById(branchCityId)
                    .orElseThrow(() -> new BusinessRegistrationException("Branch city not found."));
        }

        BusinessBranch branch = new BusinessBranch();
        branch.setBusiness(business);
        branch.setCity(city);
        branch.setName(branchName);
        branch.setAddress(branchAddress);
        branch.setOnlineOnly(onlineOnly);
        branch.setStatus(RecordStatus.ACTIVE);
        branch = businessBranchRepository.save(branch);

        BusinessMember member = new BusinessMember();
        member.setBusiness(business);
        member.setUser(owner);
        member.setRole(BusinessMemberRole.OWNER);
        member.setStatus(RecordStatus.ACTIVE);
        member = businessMemberRepository.save(member);

        BusinessContact contact = null;
        if (contactEmail != null) {
            contact = new BusinessContact();
            contact.setBusiness(business);
            contact.setBranch(branch);
            contact.setContactType(ContactType.EMAIL);
            contact.setContactValue(contactEmail);
            contact.setPrimaryContact(true);
            contact.setStatus(RecordStatus.ACTIVE);
            contact = businessContactRepository.save(contact);
        } else if (contactPhone != null) {
            contact = new BusinessContact();
            contact.setBusiness(business);
            contact.setBranch(branch);
            contact.setContactType(ContactType.PHONE);
            contact.setContactValue(contactPhone);
            contact.setPrimaryContact(true);
            contact.setStatus(RecordStatus.ACTIVE);
            contact = businessContactRepository.save(contact);
        }

        BusinessRegistrationResult result = new BusinessRegistrationResult();
        result.business = business;
        result.branch = branch;
        result.member = member;
        result.contact = contact;
        return result;
    }

    public BusinessRegistrationResult findByOwner(UUID userId) {
        BusinessMember member = businessMemberRepository.findByUserIdAndRoleAndStatus(
                userId, BusinessMemberRole.OWNER, RecordStatus.ACTIVE);
        if (member == null) return null;
        BusinessRegistrationResult result = new BusinessRegistrationResult();
        result.business = member.getBusiness();
        result.member = member;
        result.branch = businessBranchRepository
                .findByBusinessIdAndStatus(member.getBusiness().getId(), RecordStatus.ACTIVE)
                .stream().findFirst().orElse(null);
        return result;
    }

    public static class BusinessRegistrationResult {
        public Business business;
        public BusinessBranch branch;
        public BusinessMember member;
        public BusinessContact contact;
    }
}
