package kz.ask.business.domain;

import java.util.UUID;
import kz.ask.business.domain.dto.BusinessContactDto;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.business.domain.entity.BusinessContact;
import kz.ask.business.domain.enums.ContactType;
import kz.ask.business.domain.enums.ContactVisibility;
import kz.ask.business.infrastructure.mapper.BusinessMapper;
import kz.ask.business.infrastructure.repository.BusinessBranchRepository;
import kz.ask.business.infrastructure.repository.BusinessContactRepository;
import kz.ask.business.infrastructure.repository.BusinessRepository;
import kz.ask.contact.domain.ContactCryptoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BusinessContactServiceImpl implements BusinessContactService {

    private static final Integer VISIBLE_PHONE_SUFFIX_LENGTH = 4;

    private final BusinessContactRepository businessContactRepository;
    private final BusinessRepository businessRepository;
    private final BusinessBranchRepository businessBranchRepository;
    private final BusinessMapper businessMapper;
    private final ContactCryptoService contactCryptoService;

    @Override
    @Transactional
    public BusinessContactDto create(UUID businessId, UUID branchId, ContactType type, String value) {
        Business business = businessRepository.getReferenceById(businessId);
        BusinessBranch branch = businessBranchRepository.getReferenceById(branchId);
        BusinessContact contact = businessMapper.toContactEntity(business, branch, type, value);
        contact.setContactHash(contactCryptoService.hmac(value));
        contact.setEncryptedValue(contactCryptoService.encrypt(value));
        contact.setDisplayValue(displayValue(type, value));
        contact.setVisibility(visibility(type));
        BusinessContact entity = businessContactRepository.save(contact);
        return businessMapper.toBusinessContactDto(entity);
    }

    private String displayValue(ContactType type, String value) {
        if (type == ContactType.PHONE && value != null && value.length() > VISIBLE_PHONE_SUFFIX_LENGTH) {
            return value.substring(0, value.length() - VISIBLE_PHONE_SUFFIX_LENGTH).replaceAll("\\d", "*")
                    + value.substring(value.length() - VISIBLE_PHONE_SUFFIX_LENGTH);
        }
        return value;
    }

    private ContactVisibility visibility(ContactType type) {
        if (type == ContactType.PHONE || type == ContactType.EMAIL) {
            return ContactVisibility.AFTER_CONTACT;
        }
        return ContactVisibility.PUBLIC;
    }
}
