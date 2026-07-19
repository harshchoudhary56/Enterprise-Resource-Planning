package com.apple.inc.user.database.mysql;

import com.apple.inc.user.constants.BeanConstants;
import io.asyncer.r2dbc.mysql.MySqlConnectionConfiguration;
import io.asyncer.r2dbc.mysql.MySqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MySqlDatabaseConfiguration {

    @Bean(name = BeanConstants.MYSQL_R2DBC_PROPERTIES)
    @ConfigurationProperties(prefix = "spring.r2dbc.mysql")
    public R2dbcProperties r2dbcProperties() {
        return new R2dbcProperties();
    }

    @Bean(name = BeanConstants.MYSQL_CONNECTION_FACTORY)
    public ConnectionFactory connectionFactory(R2dbcProperties properties) {
        MySqlConnectionConfiguration config = MySqlConnectionConfiguration.builder()
                .host(properties.getUrl() != null ? extractHost(properties.getUrl()) : "localhost")
                .port(extractPort(properties.getUrl()))
                .database(extractDatabase(properties.getUrl()))
                .user(properties.getUsername())
                .password(properties.getPassword())
                .build();
        return MySqlConnectionFactory.from(config);
    }

    private String extractHost(String url) {
        // r2dbc:mysql://host:port/database
        String stripped = url.replaceFirst("r2dbc:mysql://", "");
        return stripped.split(":")[0].split("/")[0];
    }

    private int extractPort(String url) {
        try {
            String stripped = url.replaceFirst("r2dbc:mysql://", "");
            String portPart = stripped.split(":")[1].split("/")[0];
            return Integer.parseInt(portPart);
        } catch (Exception e) {
            return 3306;
        }
    }

    private String extractDatabase(String url) {
        String stripped = url.replaceFirst("r2dbc:mysql://", "");
        String[] parts = stripped.split("/");
        return parts.length > 1 ? parts[1].split("\\?")[0] : "";
    }
}
