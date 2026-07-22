package kz.ask.managedimport.api.dto;

import kz.ask.business.core.domain.enums.BusinessScope;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ManagedImportAccessResponse {

    private Boolean allowed;
    private BusinessScope businessScope;
}
