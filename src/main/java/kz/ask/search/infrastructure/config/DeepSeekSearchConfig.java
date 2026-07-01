package kz.ask.search.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class DeepSeekSearchConfig {

    @Bean
    public RestClient deepSeekRestClient(RestClient.Builder builder,
                                         @Value("${ask.ai.search.base-url:https://api.deepseek.com}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }
}
