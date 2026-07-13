package kz.ask.business.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import kz.ask.business.api.dto.ShippingResponse;
import kz.ask.business.api.dto.UpdateShippingRequest;
import kz.ask.business.domain.BusinessService;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.infrastructure.repository.BusinessRepository;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import kz.ask.shared.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ShippingProcessor {

    private final BusinessRepository businessRepository;
    private final BusinessService businessService;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public ShippingResponse getShipping(AskPrincipal principal, UUID businessId) {
        verifyOwnerAccess(principal.getUserId(), businessId);
        Business business = requireBusiness(businessId);

        List<String> cityIds = parseCityIds(business.getShippingCityIds());

        return ShippingResponse.builder()
                .shippingMode(business.getShippingMode())
                .shippingCityIds(cityIds)
                .build();
    }

    @Transactional
    public ShippingResponse updateShipping(AskPrincipal principal, UUID businessId, UpdateShippingRequest req) {
        verifyOwnerAccess(principal.getUserId(), businessId);
        Business business = requireBusiness(businessId);

        if (req.getShippingMode() != null) {
            business.setShippingMode(req.getShippingMode());
        }

        if (req.getShippingCityIds() != null) {
            try {
                business.setShippingCityIds(objectMapper.writeValueAsString(req.getShippingCityIds()));
            } catch (JsonProcessingException e) {
                business.setShippingCityIds(null);
            }
        }

        List<String> cityIds = parseCityIds(business.getShippingCityIds());

        return ShippingResponse.builder()
                .shippingMode(business.getShippingMode())
                .shippingCityIds(cityIds)
                .build();
    }

    private void verifyOwnerAccess(UUID userId, UUID businessId) {
        if (!businessService.isOwnerOfBusiness(businessId, userId)) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
    }

    private Business requireBusiness(UUID businessId) {
        return businessRepository.findById(businessId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.BUSINESS_NOT_FOUND));
    }

    private List<String> parseCityIds(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }
}
