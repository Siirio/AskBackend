package kz.ask.offer.purchase.infrastructure.mapper;

import java.util.List;
import kz.ask.offer.purchase.api.dto.PurchaseDestinationRequest;
import kz.ask.offer.purchase.api.dto.PurchaseDestinationResponse;
import kz.ask.offer.purchase.domain.PurchaseDestination;
import kz.ask.offer.purchase.domain.PurchaseDestinationDto;
import org.springframework.stereotype.Component;

@Component
public class PurchaseDestinationMapper {

    public List<PurchaseDestination> toEntities(List<PurchaseDestinationRequest> requests) {
        if (requests == null) {
            return List.of();
        }
        return requests.stream().map(request -> {
            PurchaseDestination destination = new PurchaseDestination();
            destination.setLabel(request.getLabel().trim());
            destination.setUrl(request.getUrl().trim());
            return destination;
        }).toList();
    }

    public List<PurchaseDestinationDto> toDtos(List<PurchaseDestination> destinations) {
        if (destinations == null) {
            return List.of();
        }
        return destinations.stream()
                .map(destination -> PurchaseDestinationDto.builder()
                        .label(destination.getLabel())
                        .url(destination.getUrl())
                        .build())
                .toList();
    }

    public List<PurchaseDestinationResponse> toResponses(List<PurchaseDestinationDto> destinations) {
        if (destinations == null) {
            return List.of();
        }
        return destinations.stream()
                .map(destination -> PurchaseDestinationResponse.builder()
                        .label(destination.getLabel())
                        .url(destination.getUrl())
                        .build())
                .toList();
    }

    public List<PurchaseDestinationResponse> entitiesToResponses(List<PurchaseDestination> destinations) {
        return toResponses(toDtos(destinations));
    }
}
