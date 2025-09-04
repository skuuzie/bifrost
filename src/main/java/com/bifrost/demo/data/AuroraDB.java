package com.bifrost.demo.data;

import com.bifrost.demo.constants.DBConstants;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static com.bifrost.demo.util.AWSUtil.generateDSQLToken;

@Component
public final class AuroraDB implements SQLDatabase {
    private volatile HikariDataSource dataSource;
    @Value("${db.aurora.cluster.endpoint}")
    private String endpoint;
    @Value("${db.aurora.cluster.region}")
    private Region region;
    private final Logger log = LoggerFactory.getLogger(AuroraDB.class);

    @PostConstruct
    public void preInitialize() {
        createNewDataSource();

        try (Connection testConn = dataSource.getConnection()) {
            initializeTables();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace(System.out);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        try {
            return dataSource.getConnection();
        } catch (SQLException first) {
            synchronized (this) {
                try {
                    return dataSource.getConnection();
                } catch (SQLException second) {
                    createNewDataSource();
                    return dataSource.getConnection();
                }
            }
        }
    }

    private void createNewDataSource() {
        String url = String.format("jdbc:postgresql://%s/postgres", this.endpoint);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername("admin");
        config.setPassword(generateDSQLToken(this.endpoint, this.region));
        config.addDataSourceProperty("ssl", "require");

        config.setMaximumPoolSize(20);
        config.setMinimumIdle(10);
        config.setConnectionTimeout(3000);
        config.setMaxLifetime(840000);

        this.dataSource = new HikariDataSource(config);

        log.info("createNewDataSource finished!");
    }

    private void initializeTables() throws SQLException {
        Connection conn = this.getConnection();
        List<String> queries = new ArrayList<>();

        queries.add("""
                    CREATE TABLE IF NOT EXISTS %s (
                        %s TEXT UNIQUE PRIMARY KEY,
                        %s TEXT UNIQUE NOT NULL,
                        %s TEXT NOT NULL,
                        %s TEXT NOT NULL,
                        %s TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        %s TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    );
                """.formatted(
                DBConstants.ParameterRegistryTable.NAME,
                DBConstants.ParameterRegistryTable.COL_ID,
                DBConstants.ParameterRegistryTable.COL_KEY,
                DBConstants.ParameterRegistryTable.COL_VALUE,
                DBConstants.ParameterRegistryTable.COL_DESCRIPTION,
                DBConstants.ParameterRegistryTable.COL_CREATED,
                DBConstants.ParameterRegistryTable.COL_UPDATED
        ));

        queries.add("""
                    CREATE TABLE IF NOT EXISTS %s (
                        %s TEXT UNIQUE PRIMARY KEY,
                        %s TEXT NOT NULL,
                        %s TEXT,
                        %s TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        %s TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    );
                """.formatted(
                DBConstants.ResumeRoasteryTable.NAME,
                DBConstants.ResumeRoasteryTable.COL_ID,
                DBConstants.ResumeRoasteryTable.COL_STATUS,
                DBConstants.ResumeRoasteryTable.COL_RESULT,
                DBConstants.ResumeRoasteryTable.COL_CREATED,
                DBConstants.ResumeRoasteryTable.COL_UPDATED
        ));

        try (Statement stmt = conn.createStatement()) {
            for (String q : queries) {
                stmt.execute(q);
            }
        } catch (SQLException e) {
            e.printStackTrace(System.out);
        }
    }
}
