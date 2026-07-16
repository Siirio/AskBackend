package kz.ask.search.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import java.time.Duration;

@Configuration
public class DeepSeekSearchConfig {

    @Bean
    public RestClient deepSeekRestClient(RestClient.Builder builder,
                                         @Value("${ask.ai.search.base-url:https://api.deepseek.com}") String baseUrl,
                                         @Value("${ask.ai.search.connect-timeout:PT2S}") Duration connectTimeout,
                                         @Value("${ask.ai.search.read-timeout:PT4S}") Duration readTimeout) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);
        return builder.baseUrl(baseUrl).requestFactory(requestFactory).build();
    }
}
