package com.bifrost.demo.service.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static com.bifrost.demo.service.util.AWSUtil.generateDSQLToken;

@Component
public final class CoreDB {
    @Value("${db.aurora.table.parameters}")
    public String PARAMETERS_TABLE_NAME;
    private HikariDataSource dataSource;
    @Value("${db.aurora.cluster.endpoint}")
    private String endpoint;
    private volatile boolean refreshing = false;
    @Value("${db.aurora.cluster.region}")
    private Region region;

    @PostConstruct
    public void preInitialize() {
        createDataSource();

        try (Connection testConn = dataSource.getConnection()) {
            initializeTables();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace(System.out);
        }
    }

    public Connection getConnection() throws SQLException {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            if (!refreshing && isConnectionError(e)) {
                refreshDataSource();
                return dataSource.getConnection();
            }
            throw e;
        }
    }

    private boolean isConnectionError(SQLException e) {
        String msg = e.getMessage().toLowerCase();
        return msg.contains("connection") ||
                msg.contains("authentication") ||
                msg.contains("password") ||
                msg.contains("token");
    }

    private synchronized void refreshDataSource() {
        if (refreshing) return;

        refreshing = true;
        try {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
            }

            createDataSource();

        } catch (Exception e) {
            e.printStackTrace(System.out);
        } finally {
            refreshing = false;
        }
    }

    private void createDataSource() {
        String url = String.format("jdbc:postgresql://%s/postgres", this.endpoint);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername("admin");
        config.setPassword(generateDSQLToken(this.endpoint, this.region));
        config.addDataSourceProperty("ssl", "require");

        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(10000);
        config.setMaxLifetime(840000);

        this.dataSource = new HikariDataSource(config);
    }

    private void initializeTables() throws SQLException {
        Connection conn = this.getConnection();
        List<String> queries = new ArrayList<>();

        queries.add(String.format("""
                    CREATE TABLE IF NOT EXISTS %s (
                        id TEXT UNIQUE PRIMARY KEY,
                        key TEXT UNIQUE NOT NULL,
                        value TEXT NOT NULL,
                        description TEXT NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    );
                """, PARAMETERS_TABLE_NAME));

        try (Statement stmt = conn.createStatement()) {
            for (String q : queries) {
                stmt.execute(q);
            }
        } catch (SQLException e) {
            e.printStackTrace(System.out);
        }
    }
}
