package kz.ask.ai.infrastructure.client;

import java.nio.charset.StandardCharsets;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ExternalServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

public abstract class BaseDeepSeekClient {

    protected static final String CHAT_COMPLETIONS_PATH = "/chat/completions";

    @Value("${ask.ai.search.api-key:}")
    protected String apiKey;

    @Value("${ask.ai.search.model:deepseek-v4-flash}")
    protected String model;

    public Boolean isAvailable() {
        return StringUtils.hasText(apiKey);
    }

    protected String readPrompt(Resource promptResource) {
        try {
            return promptResource.getContentAsString(StandardCharsets.UTF_8);
        } catch (java.io.IOException e) {
            throw new ExternalServiceException(ErrorCode.AI_INTENT_STRUCTURE_FAILED);
        }
    }
}
