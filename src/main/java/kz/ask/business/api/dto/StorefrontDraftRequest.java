package kz.ask.business.api.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StorefrontDraftRequest {
    private List<StorefrontBlockRequest> blocks;
}
