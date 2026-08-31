package com.hmg.role.common.cdc.debezium;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DebeziumPreflightValidator {
    private static final Pattern TABLE_NAME_PATTERN =
            Pattern.compile("[^A-Za-z0-9_]+"); // NOSONAR: everything should be ASCII

    /**
     * Validate that: - all tables in tableInclude exist (db.table form for MySQL) - we can SELECT
     * from each table (privileges) - binlog_format is ROW (optional but recommended) Throws
     * IllegalStateException if any check fails.
     */
    public void validateOrThrow(Properties props) {
        String jdbcUrl = props.getProperty("jdbc.url");
        String user = props.getProperty("database.user");
        String password = props.getProperty("database.password");

        String dbNameCsv = props.getProperty("database.include.list");
        String tableIncludeCsv = props.getProperty("table.include.list");

        final List<String> tables =
                Arrays.stream(tableIncludeCsv.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isBlank())
                        .toList();

        if (tables.isEmpty()) {
            throw new IllegalStateException(
                    "Debezium table.include.list is empty; refuse to start.");
        }

        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password)) {
            conn.setReadOnly(true);

            // Optional: check binlog_format
            // Debezium requires ROW binlog format.
            // [1](https://debezium.io/documentation/reference/stable/connectors/mysql.html)
            validateBinlogAccess(conn);

            validateDbzDatabase(dbNameCsv, tables);

            for (String fqtn : tables) {
                // MySQL: db.table (schema == database)
                // [4](http://debezium.io/blog/2025/10/06/add-new-table-to-capture-list/)
                validateDbzTables(fqtn, conn);
            }

            // Optional: verify Debezium grants exist (SHOW GRANTS).
            // [3](https://github.com/debezium/debezium/blob/main/debezium-core/src/main/java/io/debezium/transforms/ExtractNewRecordState.java)
            validateReplicationAccess(conn);

            log.info(
                    "Debezium preflight OK: found {} tables and privileges are sufficient.",
                    tables.size());
        } catch (SQLException e) {
            throw new IllegalStateException("Preflight validation failed: " + e.getMessage(), e);
        }
    }

    private void validateBinlogAccess(Connection conn) throws SQLException {
        try (PreparedStatement ps =
                conn.prepareStatement("SHOW GLOBAL VARIABLES LIKE 'binlog_format'")) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String fmt = rs.getString("Value");
                    if (!"ROW".equalsIgnoreCase(fmt)) {
                        throw new IllegalStateException(
                                "MySQL binlog_format must be ROW, found: " + fmt);
                    }
                }
            }
        }
    }

    private void validateDbzDatabase(String dbName, List<String> tables) {
        List<String> tableDbNames = tables.stream().map(k -> k.split("\\.")[0]).distinct().toList();

        if (tableDbNames.isEmpty()) {
            log.warn("Specified debezium table names isn't in FTDN");
        }

        if (tableDbNames.size() != 1) {
            log.warn("Multiple table names specified for Debezium");
        }

        if (!tableDbNames.contains(dbName)) {
            log.warn(
                    "Table names specified for Debezium different from application database name. Database name for application: {}, database name specified for Debezium: {}",
                    dbName,
                    String.join(", ", tableDbNames));
        }
    }

    private void validateDbzTables(String fqtn, Connection conn) throws SQLException {
        String[] parts = fqtn.split("\\.");
        if (parts.length != 2) {
            throw new IllegalStateException(
                    "Invalid MySQL table identifier '%s'. Expected 'database.table'."
                            .formatted(fqtn));
        }
        String db = sanitize(parts[0]);
        String table = sanitize(parts[1]);

        // Exists in INFORMATION_SCHEMA.TABLES?
        try (PreparedStatement ps =
                conn.prepareStatement(
                        "SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA=? AND TABLE_NAME=?")) {
            ps.setString(1, db);
            ps.setString(2, table);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("Target table not found: " + fqtn);
                }
            }
        }

        // Privilege: attempt a harmless SELECT with LIMIT 1
        try (Statement st = conn.createStatement()) {
            st.execute(
                    // the variables are from configs and has been sanitized
                    "SELECT 1 FROM `" + db + "`.`" + table + "` LIMIT 1"); // NOSONAR
        } catch (SQLException ex) {
            throw new IllegalStateException(
                    "Debezium has no SELECT privilege on %s': %s".formatted(fqtn, ex.getMessage()),
                    ex);
        }
    }

    private static void validateReplicationAccess(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("SHOW GRANTS")) {
            boolean hasReplication = false, hasShowDb = false, hasReload = false;
            while (rs.next()) {
                String grant = rs.getString(1);
                if (grant.contains("REPLICATION SLAVE") || grant.contains("REPLICATION CLIENT"))
                    hasReplication = true;
                if (grant.contains("SHOW DATABASES")) hasShowDb = true;
                if (grant.contains("RELOAD")) hasReload = true;
            }
            if (!(hasReplication && hasShowDb && hasReload)) {
                throw new IllegalStateException(
                        "Missing required Debezium grants (SELECT, RELOAD, SHOW DATABASES, REPLICATION SLAVE/CLIENT).");
            }
        }
    }

    private static String sanitize(String text) {
        // prevent SQL injection attack
        Matcher matcher = TABLE_NAME_PATTERN.matcher(text);
        return matcher.replaceAll("");
    }
}
