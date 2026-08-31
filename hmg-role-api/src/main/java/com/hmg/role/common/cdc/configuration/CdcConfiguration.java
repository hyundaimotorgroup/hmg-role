package com.hmg.role.common.cdc.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmg.role.common.cdc.eventhandler.CdcEventHandler;
import com.hmg.role.common.cdc.eventhandler.components.CsvHeaderExtractor;
import com.hmg.role.common.cdc.eventhandler.components.CsvRowBuilder;
import com.hmg.role.common.cdc.eventhandler.components.ProjectMetadataService;
import com.hmg.role.common.cdc.io.FileManager;
import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class CdcConfiguration {

    @Value("${cdc.enabled:false}")
    private boolean cdcEnabled;

    @Value("${cdc.base-working-path:tmpdir}")
    private String cdcBasePath;

    @Bean
    public CdcEventHandler cdcEventHandler(
            ApplicationEventPublisher eventPublisher,
            ObjectMapper mapper,
            FileManager fileManager,
            ScheduledExecutorService debounceScheduler,
            CsvHeaderExtractor headerExtractor,
            CsvRowBuilder rowBuilder,
            ProjectMetadataService metadataService,
            @Value("${cdc.timer-grace-millis:100}") long debounceMillis) {
        if (!cdcEnabled) {
            log.warn("CDC configuration is missing or set to OFF");
        } else {
            log.info("Starting CDC event handler");
        }

        return new CdcEventHandler(
                eventPublisher,
                mapper,
                fileManager,
                debounceScheduler,
                headerExtractor,
                rowBuilder,
                metadataService,
                debounceMillis);
    }

    @Bean
    public FileManager fileManager() throws IOException {
        return FileManager.builder().baseWorkingPath(cdcBasePath).build();
    }

    @Bean
    public CsvHeaderExtractor csvHeaderExtractor() {
        return new CsvHeaderExtractor();
    }

    @Bean
    public CsvRowBuilder csvRowBuilder() {
        return new CsvRowBuilder();
    }

    @Bean
    public ProjectMetadataService projectMetadataService(FileManager fm) {
        return new ProjectMetadataService(fm);
    }

    // Virtual-thread scheduled executor (timers only)
    @Bean
    public ScheduledExecutorService debounceScheduler() {
        return new ScheduledThreadPoolExecutor(
                Math.max(2, Runtime.getRuntime().availableProcessors()),
                Thread.ofVirtual().name("cdc-debounce-", 0).factory());
    }
}
