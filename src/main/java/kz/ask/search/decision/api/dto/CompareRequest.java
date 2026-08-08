package kz.ask.search.decision.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import kz.ask.search.basic.api.dto.DecisionContextRequest;
import kz.ask.search.basic.domain.enums.SearchScope;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompareRequest {

    @NotNull
    private SearchScope mode;

    @NotNull
    @Size(min = 2, max = 5)
    private List<UUID> resultIds;

    @Valid
    private DecisionContextRequest decisionContext;

    @Size(max = 16)
    private String locale;
}
