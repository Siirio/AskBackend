package kz.ask.search.infrastructure.config;

import kz.ask.search.domain.SearchReindexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ask.search.reindex.on-startup", havingValue = "true")
public class SearchMaintenanceRunner implements ApplicationRunner {

    private final SearchReindexService reindexService;

    @Override
    public void run(ApplicationArguments args) {
        Long indexed = reindexService.rebuildMeilisearch();
        log.info("Search startup reindex completed documents={}", indexed);
    }
}
