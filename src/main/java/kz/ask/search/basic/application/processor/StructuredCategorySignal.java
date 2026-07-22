package kz.ask.search.basic.application.processor;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StructuredCategorySignal {

    private String canonicalKey;
    private List<String> aliases;
    private Double confidence;
}
