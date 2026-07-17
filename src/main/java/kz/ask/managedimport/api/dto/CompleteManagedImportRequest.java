package kz.ask.managedimport.api.dto;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompleteManagedImportRequest {

    @Min(0)
    private Integer productsPublishedCount = 0;
}
