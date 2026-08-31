package com.hmg.role.common.cdc.configuration;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Properties;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnExpression("T(java.lang.Boolean).valueOf('${cdc.enabled:false}')")
public class CdcPropertiesFactory {
    private static final String FILE_OFFSET_STORAGE =
            "org.apache.kafka.connect.storage.FileOffsetBackingStore";
    private static final String SCHEMA_HISTORY_IMPL =
            "io.debezium.storage.file.history.FileSchemaHistory";

    @Value("${debezium.mysql.connector}")
    private String dbzConnector;

    @Value("${cdc.base-working-path:tmpdir}")
    private String baseWorkingPath;

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${debezium.mysql.user}")
    private String user;

    @Value("${debezium.mysql.password}")
    private String password;

    @Value("${debezium.mysql.server-id}")
    private String serverId;

    @Value("${debezium.topic-prefix}")
    private String topicPrefix;

    @Value("${debezium.mysql.target-table}")
    private String tableInclude;

    @Value("${debezium.mysql.snapshot-mode:initial}")
    private String snapshotMode;

    @Value("${debezium.mysql.snapshot-locking-mode:none}")
    private String snapshotLockingMode;

    @Value("${debezium.mysql.include-schema-changes:false}")
    private String includeSchemaChanges;

    // currently unused. To be updated if offset storage to be moved online (S3 or somewhere)
    //    @Value("${debezium.engine.offset-storage}")
    //    private String offsetStorage;

    // internally configured for now
    //    @Value("${debezium.engine.offset-file}")
    //    private String offsetFile;

    @Value("${debezium.engine.offset-flush-interval-ms:6000}")
    private String offsetFlushMs;

    //    @Value("${debezium.engine.schema-history-internal}")
    //    private String schemaHistoryImpl;

    // internally configured for now
    //    @Value("${debezium.engine.schema-history-file}")
    //    private String schemaHistoryFile;

    @Value("${debezium.mysql.unwrap.enabled:true}")
    private boolean unwrapEnabled;

    @Value("${debezium.mysql.unwrap.add-fields:op,table,ts_ms}")
    private String unwrapAddFields;

    @Value("${debezium.mysql.unwrap.drop-tombstones:true}")
    private boolean unwrapDropTombstones;

    public Properties getProperties() throws IOException {
        Properties props = new Properties();

        // Strip "jdbc:" so we can parse it with URI
        URI uri = URI.create(jdbcUrl.substring("jdbc:".length()));
        String host = uri.getHost();
        int port = (uri.getPort() == -1) ? 3306 : uri.getPort();
        String targetDb = uri.getPath().replaceFirst("/", "");

        props.setProperty("jdbc.url", jdbcUrl);

        Path basePath = getBasePath();

        // --- ENGINE STATE STORES (required in embedded mode) ---
        props.setProperty("name", "mysql-embedded-cdc");
        props.setProperty("offset.storage", FILE_OFFSET_STORAGE);
        props.setProperty("offset.storage.file.filename", getOffsetFile(basePath).toString());
        props.setProperty("offset.flush.interval.ms", offsetFlushMs);

        // schema history for MySQL (internal history of DDL)
        props.setProperty("schema.history.internal", SCHEMA_HISTORY_IMPL);
        props.setProperty(
                "schema.history.internal.file.filename", getSchemaHistoryFile(basePath).toString());

        // --- SOURCE CONNECTOR (MySQL) ---
        props.setProperty("connector.class", dbzConnector);
        props.setProperty("database.hostname", host);
        props.setProperty("database.port", Integer.toString(port));
        props.setProperty("database.user", user);
        props.setProperty("database.password", password);
        props.setProperty("database.server.id", serverId);

        props.setProperty("topic.prefix", topicPrefix);

        // include lists
        props.setProperty("database.include.list", targetDb);
        props.setProperty("table.include.list", tableInclude);

        // snapshot behavior
        props.setProperty("snapshot.mode", snapshotMode);
        props.setProperty("snapshot.locking.mode", snapshotLockingMode);
        props.setProperty("include.schema.changes", includeSchemaChanges);

        // --- Single Message Transform (unwrap) ---
        if (unwrapEnabled) {
            props.setProperty("transforms", "unwrap");
            props.setProperty(
                    "transforms.unwrap.dataType", "io.debezium.transforms.ExtractNewRecordState");
            props.setProperty("transforms.unwrap.add.fields", unwrapAddFields);
            props.setProperty(
                    "transforms.unwrap.drop.tombstones", Boolean.toString(unwrapDropTombstones));
        }
        return props;
    }

    private Path getBasePath() throws IOException {
        String basePathStr;
        if (baseWorkingPath == null || "tmpdir".equals(baseWorkingPath)) {
            basePathStr = System.getProperty("java.io.tmpdir");
        } else {
            basePathStr = baseWorkingPath;
        }

        String randomUuid = UUID.randomUUID().toString();
        Path basePath = Paths.get(basePathStr, "hmgrole", "cdc-" + randomUuid);
        Files.createDirectories(basePath);
        return basePath;
    }

    private Path getOffsetFile(Path basePath) {
        Path offsetFile = basePath.resolve("offset.dat");
        File file = offsetFile.toFile();
        file.setLastModified(Instant.now().toEpochMilli()); // NOSONAR
        log.info("creating Debezium offset.dat file in path: {}", offsetFile.toString());
        return offsetFile;
    }

    private Path getSchemaHistoryFile(Path basePath) {
        Path schemaHistoryFile = basePath.resolve("schema-history.dat");
        File file = schemaHistoryFile.toFile();
        file.setLastModified(Instant.now().toEpochMilli()); // NOSONAR
        log.info(
                "creating Debezium schema-history.dat file in path: {}",
                schemaHistoryFile.toString());
        return schemaHistoryFile;
    }
}
