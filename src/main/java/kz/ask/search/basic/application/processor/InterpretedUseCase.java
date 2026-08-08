package kz.ask.search.basic.application.processor;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InterpretedUseCase {

    private String key;
    private String label;
}
