package com.hmg.role.common.cdc.debezium;

import com.hmg.role.common.cdc.configuration.CdcPropertiesFactory;
import com.hmg.role.common.cdc.eventhandler.CdcEventHandler;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.Json;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnExpression("T(java.lang.Boolean).valueOf('${cdc.enabled:false}')")
@RequiredArgsConstructor
public class DebeziumEngineFactory implements DisposableBean {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private DebeziumEngine<ChangeEvent<String, String>> engine;

    private final CdcEventHandler eventHandler;
    private final DebeziumPreflightValidator validator;
    private final CdcPropertiesFactory cdcPropertiesFactory;

    @Bean
    public DebeziumEngine<ChangeEvent<String, String>> debeziumEngine() throws IOException {
        Properties props = cdcPropertiesFactory.getProperties();
        log.info(
                "Debezium is configured to use offset.storage: {}, offset.file: {}",
                props.getProperty("offset.storage"),
                props.getProperty("offset.file"));

        validator.validateOrThrow(props);

        log.info("Starting debezium engine with properties: {}", props);
        // Build the engine with JSON output; AsyncEmbeddedEngine is the default impl since 3.2+
        engine =
                DebeziumEngine.create(Json.class)
                        .using(getClass().getClassLoader())
                        .using(props)
                        .notifying(eventHandler::handleEvent)
                        .using(
                                (success, message, error) -> {
                                    log.info(
                                            "Debezium engine message: success={}, message={}",
                                            success,
                                            message);
                                    if (!success) {
                                        log.error("Debezium failure: {}", message, error);
                                    }
                                })
                        .build();

        executor.submit(engine); // start engine on a background thread
        log.info("Debezium engine created");
        return engine;
    }

    @Override
    public void destroy() throws Exception {
        if (engine != null) engine.close(); // triggers offset flush & graceful stop
        executor.shutdown();
    }
}
