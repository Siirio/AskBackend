package kz.ask.autodump.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class DeepSeekAutodumpConfig {

    @Bean
    public RestClient autodumpRestClient(RestClient.Builder builder,
                                         @Value("${ask.ai.autodump.base-url:https://api.deepseek.com}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }
}
