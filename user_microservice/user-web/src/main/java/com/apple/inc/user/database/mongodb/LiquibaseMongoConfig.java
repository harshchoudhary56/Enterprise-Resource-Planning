package com.apple.inc.user.database.mongodb;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * Programmatic Liquibase runner for MongoDB in the user microservice.
 *
 * <p><b>Why is this needed?</b><br>
 * Spring Boot's {@code LiquibaseAutoConfiguration} only supports JDBC datasources.
 * The {@code liquibase-mongodb} extension provides a custom {@code MongoLiquibaseDatabase}
 * implementation, but Spring Boot has no auto-configuration for it.
 * So we must run Liquibase programmatically via a {@link CommandLineRunner}.</p>
 *
 * <p><b>How it works:</b><br>
 * 1. {@code DatabaseFactory} detects the {@code mongodb://} URI scheme and
 *    creates a {@code MongoLiquibaseDatabase} instance (from liquibase-mongodb extension).<br>
 * 2. Liquibase reads the master changelog and applies pending changesets.<br>
 * 3. It tracks applied changesets in MongoDB's own {@code DATABASECHANGELOG} collection.</p>
 *
 * <p>The MySQL Liquibase migrations continue to run via Spring Boot's
 * standard auto-configuration — this config is only for MongoDB.</p>
 *
 * <p>Can be disabled by setting {@code spring.liquibase.mongodb.enabled=false}
 * in application properties (useful for tests).</p>
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "spring.liquibase.mongodb.enabled", havingValue = "true", matchIfMissing = true)
public class LiquibaseMongoConfig {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.liquibase.mongodb.change-log}")
    private String changeLogFile;

    @Bean
    @Order(1) // Run before other CommandLineRunners — schema must exist before app logic
    public CommandLineRunner liquibaseMongoRunner() {
        return args -> {
            log.info("Running Liquibase MongoDB migrations...");
            try {
                Database database = DatabaseFactory.getInstance()
                        .openDatabase(mongoUri, null, null, null, new ClassLoaderResourceAccessor());

                try (Liquibase liquibase = new Liquibase(
                        changeLogFile,
                        new ClassLoaderResourceAccessor(),
                        database)) {
                    liquibase.update("");
                }

                log.info("Liquibase MongoDB migrations completed successfully.");
            } catch (Exception e) {
                log.error("Liquibase MongoDB migration failed", e);
                throw new RuntimeException("Liquibase MongoDB migration failed", e);
            }
        };
    }
}
