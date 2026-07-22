package kz.ask.business.domain;

import kz.ask.business.domain.dto.BusinessBranchDto;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.business.infrastructure.mapper.BusinessMapper;
import kz.ask.business.infrastructure.repository.BusinessBranchRepository;
import kz.ask.business.infrastructure.repository.BusinessRepository;
import kz.ask.business.infrastructure.repository.CityRepository;
import kz.ask.shared.domain.entity.City;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BusinessBranchServiceImpl implements BusinessBranchService {

    private final BusinessBranchRepository businessBranchRepository;
    private final BusinessRepository businessRepository;
    private final CityRepository cityRepository;
    private final CityService cityService;
    private final BusinessMapper businessMapper;

    @Override
    @Transactional(readOnly = true)
    public BusinessBranchDto findById(UUID branchId) {
        BusinessBranch entity = businessBranchRepository.findById(branchId).orElse(null);
        return entity != null ? businessMapper.toBusinessBranchDto(entity) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessBranchDto findByBusinessAndId(UUID businessId, UUID branchId) {
        BusinessBranch branch = businessBranchRepository.findById(branchId).orElse(null);
        if (branch == null || !branch.getBusiness().getId().equals(businessId)) {
            return null;
        }
        return businessMapper.toBusinessBranchDto(branch);
    }

    @Override
    @Transactional
    public BusinessBranchDto create(UUID businessId, UUID cityId, String name, String address, String addressDetails,
                                     BigDecimal latitude, BigDecimal longitude) {
        Business business = businessRepository.getReferenceById(businessId);
        City city = null;
        if (cityId != null) {
            cityService.findById(cityId);
            city = cityRepository.getReferenceById(cityId);
        }
        BusinessBranch entity = businessBranchRepository.save(
                businessMapper.toBranchEntity(business, city, name, address, addressDetails, latitude, longitude));
        return businessMapper.toBusinessBranchDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessBranchDto findFirstByBusinessId(UUID businessId) {
        BusinessBranch branch = businessBranchRepository
                .findByBusinessId(businessId)
                .stream().findFirst().orElse(null);
        return branch != null ? businessMapper.toBusinessBranchDto(branch) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusinessBranchDto> listByBusiness(UUID businessId) {
        return businessBranchRepository.findByBusinessId(businessId)
                .stream()
                .map(businessMapper::toBusinessBranchDto)
                .toList();
    }

    @Override
    @Transactional
    public BusinessBranchDto update(UUID branchId, String name, String address, String addressDetails, UUID cityId,
                                     BigDecimal latitude, BigDecimal longitude) {
        BusinessBranch branch = businessBranchRepository.findById(branchId).orElse(null);
        if (branch == null) return null;
        if (name != null) branch.setName(name);
        if (address != null) branch.setAddress(address);
        if (addressDetails != null) branch.setAddressDetails(addressDetails);
        if (cityId != null) {
            cityService.findById(cityId);
            branch.setCity(cityRepository.getReferenceById(cityId));
        }
        if (latitude != null) branch.setLatitude(latitude);
        if (longitude != null) branch.setLongitude(longitude);
        return businessMapper.toBusinessBranchDto(branch);
    }
}
