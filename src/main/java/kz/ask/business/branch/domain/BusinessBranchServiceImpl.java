package kz.ask.business.branch.domain;

import kz.ask.business.branch.domain.dto.BusinessBranchDto;
import kz.ask.business.branch.domain.dto.SpecialOpeningIntervalDto;
import kz.ask.business.branch.domain.dto.WeeklyOpeningIntervalDto;
import kz.ask.business.core.domain.entity.Business;
import kz.ask.business.branch.domain.entity.BusinessBranch;
import kz.ask.business.branch.domain.entity.SpecialOpeningInterval;
import kz.ask.business.branch.domain.entity.WeeklyOpeningInterval;
import kz.ask.business.core.infrastructure.mapper.BusinessMapper;
import kz.ask.business.branch.infrastructure.repository.BusinessBranchRepository;
import kz.ask.business.core.infrastructure.repository.BusinessRepository;
import kz.ask.shared.domain.CityService;
import kz.ask.shared.domain.entity.City;
import kz.ask.shared.infrastructure.repository.CityRepository;
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
    @Transactional(readOnly = true)
    public Boolean allBelongToBusiness(UUID businessId, List<UUID> branchIds) {
        return branchIds.isEmpty()
                || businessBranchRepository.countByIdInAndBusinessId(branchIds, businessId) == branchIds.size();
    }

    @Override
    @Transactional
    public BusinessBranchDto create(UUID businessId, UUID cityId, String name, String address, String addressDetails,
                                     BigDecimal latitude, BigDecimal longitude,
                                     String timeZoneId,
                                     List<WeeklyOpeningIntervalDto> weeklyHours,
                                     List<SpecialOpeningIntervalDto> specialHours) {
        Business business = businessRepository.getReferenceById(businessId);
        City city = null;
        if (cityId != null) {
            cityService.findById(cityId);
            city = cityRepository.getReferenceById(cityId);
        }
        BusinessBranch entity = businessMapper.toBranchEntity(business, city, name, address, addressDetails,
                latitude, longitude, timeZoneId);
        applySchedule(entity, weeklyHours, specialHours);
        entity = businessBranchRepository.save(entity);
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
                                     BigDecimal latitude, BigDecimal longitude,
                                     String timeZoneId,
                                     List<WeeklyOpeningIntervalDto> weeklyHours,
                                     List<SpecialOpeningIntervalDto> specialHours) {
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
        if (timeZoneId != null) branch.setTimeZoneId(timeZoneId);
        if (weeklyHours != null) {
            branch.getWeeklyHours().clear();
            branch.getWeeklyHours().addAll(
                    weeklyHours.stream().map(this::toWeeklyEntity).toList());
        }
        if (specialHours != null) {
            branch.getSpecialHours().clear();
            branch.getSpecialHours().addAll(
                    specialHours.stream().map(this::toSpecialEntity).toList());
        }
        return businessMapper.toBusinessBranchDto(branch);
    }

    private void applySchedule(BusinessBranch entity,
                                List<WeeklyOpeningIntervalDto> weeklyHours,
                                List<SpecialOpeningIntervalDto> specialHours) {
        if (weeklyHours != null) {
            entity.setWeeklyHours(weeklyHours.stream().map(this::toWeeklyEntity).toList());
        }
        if (specialHours != null) {
            entity.setSpecialHours(specialHours.stream().map(this::toSpecialEntity).toList());
        }
    }

    private WeeklyOpeningInterval toWeeklyEntity(WeeklyOpeningIntervalDto dto) {
        WeeklyOpeningInterval entity = new WeeklyOpeningInterval();
        entity.setDayOfWeek(dto.getDayOfWeek());
        entity.setOpensAt(dto.getOpensAt());
        entity.setClosesAt(dto.getClosesAt());
        return entity;
    }

    private SpecialOpeningInterval toSpecialEntity(SpecialOpeningIntervalDto dto) {
        SpecialOpeningInterval entity = new SpecialOpeningInterval();
        entity.setDate(dto.getDate());
        entity.setClosed(dto.getClosed());
        entity.setOpensAt(dto.getOpensAt());
        entity.setClosesAt(dto.getClosesAt());
        return entity;
    }
}
