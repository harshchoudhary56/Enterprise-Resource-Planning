package com.apple.inc.user.database.mysql;

import com.apple.inc.user.constants.BeanConstants;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableR2dbcAuditing
@EnableTransactionManagement
@EnableR2dbcRepositories(
        basePackages = "com.apple.inc.user.repository.mysql"
)
public class MySqlR2dbcConfiguration {

    @Bean(name = BeanConstants.MYSQL_DATABASE_CLIENT)
    public DatabaseClient databaseClient(@Qualifier(BeanConstants.MYSQL_CONNECTION_FACTORY) ConnectionFactory connectionFactory) {
        return DatabaseClient.create(connectionFactory);
    }

    @Bean(name = BeanConstants.MYSQL_TRANSACTION_MANAGER)
    public ReactiveTransactionManager transactionManager(@Qualifier(BeanConstants.MYSQL_CONNECTION_FACTORY) ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }
}

