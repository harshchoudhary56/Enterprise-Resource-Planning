package com.apple.inc.user.database.mongodb;

import com.apple.inc.user.constants.BeanConstants;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.ReactiveMongoTransactionManager;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory;

@Configuration
public class MongoDatabaseConfiguration {

    @Value("${spring.data.mongodb.uri://localhost:27017/power_school_user_dev}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database:power_school_user_dev}")
    private String databaseName;

    @Bean(name = BeanConstants.MONGODB_CLIENT)
    public MongoClient reactiveMongoClient() {
        return MongoClients.create(mongoUri);
    }

    @Bean(name = BeanConstants.MONGODB_DATASOURCE)
    public ReactiveMongoDatabaseFactory reactiveMongoDatabaseFactory(
            @Qualifier(BeanConstants.MONGODB_CLIENT) MongoClient mongoClient) {
        return new SimpleReactiveMongoDatabaseFactory(mongoClient, databaseName);
    }

    @Bean(name = BeanConstants.REACTIVE_MONGO_TEMPLATE)
    public ReactiveMongoTemplate reactiveMongoTemplate(
            @Qualifier(BeanConstants.MONGODB_DATASOURCE) ReactiveMongoDatabaseFactory factory) {
        return new ReactiveMongoTemplate(factory);
    }

    @Bean(name = BeanConstants.MONGODB_TRANSACTION_MANAGER)
    public ReactiveMongoTransactionManager reactiveMongoTransactionManager(
            @Qualifier(BeanConstants.MONGODB_DATASOURCE) ReactiveMongoDatabaseFactory factory) {
        return new ReactiveMongoTransactionManager(factory);
    }
}
