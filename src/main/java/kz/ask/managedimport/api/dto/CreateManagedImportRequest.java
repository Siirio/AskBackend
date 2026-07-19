package kz.ask.managedimport.api.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.LinkedHashSet;
import java.util.Set;
import kz.ask.business.domain.enums.CatalogSourceType;
import kz.ask.business.domain.enums.CatalogScope;
import kz.ask.business.domain.enums.PreferredContactChannel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateManagedImportRequest {

    @NotNull
    private CatalogScope catalogScope;

    @NotEmpty
    private Set<CatalogSourceType> sourceTypes = new LinkedHashSet<>();

    @NotNull
    private PreferredContactChannel preferredContactChannel;

    @NotBlank
    private String preferredContactValue;

    private String sourceLinks;

    private String sourceNotes;

    private String countryCode = "KZ";

    private String locale = "ru";

    private Boolean legalAccepted;

    @AssertTrue(message = "Managed import terms must be accepted")
    public boolean legalAccepted() {
        return Boolean.TRUE.equals(legalAccepted);
    }
}
