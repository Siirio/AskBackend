package kz.ask.managedimport.api.dto;

import kz.ask.business.domain.enums.CatalogScope;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ManagedImportAccessResponse {

    private Boolean allowed;
    private CatalogScope catalogScope;
}
