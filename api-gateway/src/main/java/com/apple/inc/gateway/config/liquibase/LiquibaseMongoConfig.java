package com.apple.inc.gateway.config.liquibase;

import com.apple.inc.gateway.config.mongodb.MongoProperties;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * Programmatic Liquibase runner for MongoDB in the API Gateway.
 *
 * <p><b>Why is this needed?</b><br>
 * Spring Boot's {@code LiquibaseAutoConfiguration} only supports JDBC datasources.
 * The {@code liquibase-mongodb} extension provides a custom {@code MongoLiquibaseDatabase}
 * implementation, but Spring Boot has no auto-configuration for it.
 * So we must run Liquibase programmatically via a {@link CommandLineRunner}.</p>
 *
 * <p>Can be disabled by setting {@code spring.liquibase.mongodb.enabled=false}.</p>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.liquibase.mongodb.enabled", havingValue = "true", matchIfMissing = true)
public class LiquibaseMongoConfig {

    private final MongoProperties properties;

    @Bean
    @Order(1)
    public CommandLineRunner liquibaseMongoRunner() {
        return args -> {
            log.info("Running Liquibase MongoDB migrations...");
            System.out.println("------------properties-----------------" + properties);
            try {
                Database database = DatabaseFactory.getInstance()
                        .openDatabase(properties.getUri(), null, null, null, new ClassLoaderResourceAccessor());

                try (Liquibase liquibase = new Liquibase(
                        "db/changelog/master-changelog.xml",
                        new ClassLoaderResourceAccessor(),
                        database)) {
                    liquibase.update("development");
                }

                log.info("Liquibase MongoDB migrations completed successfully.");
            } catch (Exception e) {
                log.error("Liquibase MongoDB migration failed", e);
                throw new RuntimeException("Liquibase MongoDB migration failed", e);
            }
        };
    }
}
