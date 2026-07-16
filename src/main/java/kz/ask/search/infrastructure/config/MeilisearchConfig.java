package kz.ask.search.infrastructure.config;

import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Config;
import com.meilisearch.sdk.json.JacksonJsonHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MeilisearchConfig {

    @Bean
    public Client meilisearchClient(@Value("${ask.ai.meilisearch.host-url:http://localhost:7700}") String hostUrl,
                                    @Value("${ask.ai.meilisearch.api-key:}") String apiKey) {
        return new Client(new Config(hostUrl, apiKey, new JacksonJsonHandler()));
    }
}
