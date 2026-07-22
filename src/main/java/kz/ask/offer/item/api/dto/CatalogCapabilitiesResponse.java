package kz.ask.offer.item.api.dto;

import java.util.Set;
import kz.ask.offer.item.domain.enums.CatalogCapability;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogCapabilitiesResponse {

    private Set<CatalogCapability> capabilities;
}
