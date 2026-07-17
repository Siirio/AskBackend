package kz.ask.managedimport.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ManagedImportAccessResponse {

    private Boolean allowed;
}
